const util = require("util");
const fs = require("fs");
const path = require("path");
const videoDir = process.env.VIDEO_DIR || path.join(__dirname, "videos");
const mkvExtract = require("./mkvExtract.js"); // Assurez-vous d'avoir cette bibliothèque
const ffmpeg = require("fluent-ffmpeg"); // Assurez-vous d'avoir cette bibliothèque
const chalk = require("chalk");
const cliProgress = require("cli-progress");

let serieToExtract;
let serieDir;

// Convertir fs.readdir en une fonction qui retourne une Promise
const readdir = util.promisify(fs.readdir);
const writeFile = util.promisify(fs.writeFile);
const unlink = util.promisify(fs.unlink);

// récupérer l'argument passé en ligne de commande
if (process.argv.length > 2) {
  serieToExtract = process.argv[2];
  serieDir = path.join(videoDir, serieToExtract);
} else {
  console.log(
    chalk.red("No serie specified\n") +
      "Usage: " +
      chalk.blue("node extractSubtitlesThumbnails.js <serie>")
  );
  return;
}
let multi = new cliProgress.MultiBar(
  {
    clearOnComplete: true,
    hideCursor: true,
    format:
      chalk.blue("{bar}") +
      " {percentage}% | {value}/{total} " +
      chalk.green("{custom}"),
  },
  cliProgress.Presets.shades_classic
);
process.on("SIGINT", () => {
  multi.stop();
  process.exit();
});

let globalBar = multi.create(2, 0, { custom: "Starting..." });
let localBar;

async function extractSubtitlesAndThumbnails() {
  try {
    const files = await readdir(serieDir);
    let fileNumber = 0;
    for (const file of files) {
      const ext = path.extname(file);
      if (ext === ".mkv") {
        fileNumber++;
      }
    }
    globalBar.update(1, {
      custom: chalk.red("Extracting subtitles and converting videos"),
    });
    localBar = multi.create(fileNumber, 0, { custom: "Starting..." });
    fileNumber = 0;
    for (const file of files) {
      const filePath = path.join(serieDir, file);
      const ext = path.extname(file);
      const fileName = path.basename(file, ext);
      if (ext === ".mkv") {
        localBar.update(fileNumber, { custom: `Treating ${fileName}` });
        // Si le fichier est un mkv, on extrait les sous-titres
        const fileStream = fs.createReadStream(filePath, {
          highWaterMark: 2 * 1024 * 1024,
        }); // Taille de chunk de 2 Mo

        let stepBar = multi.create(2, 0, {
          custom: `Extracting subtitles from ${fileName}`,
        });
        // Extraire les sous-titres
        const extractSubtitles = util.promisify(mkvExtract.mkvExtract);
        const files = await extractSubtitles(fileStream);
        files.forEach(async (f) => {
          if (f.name.endsWith(".ass") || f.name.endsWith(".ssa")) {
            const outputPath = path.join(serieDir, fileName + "." + f.name);
            try {
              writeFile(outputPath, f.data);
            } catch (err) {
              console.error(`Error writing file: ${err}`);
            }
          }
        });
        stepBar.update(1, { custom: `Converting video ${fileName} to mp4` });

        // Convertir la vidéo
        const outputFilePath = path.join(serieDir, `${fileName}.mp4`);

        let fileBar = multi.create(100, 0, {
          custom: `Converting ${fileName}...`,
        });
        await new Promise((resolve, reject) => {
          ffmpeg(filePath)
            .output(outputFilePath)
            .videoCodec("libx264")
            .audioCodec("aac")
            .addOption("-pix_fmt", "yuv420p")
            .addOption("-profile:v", "high")
            .addOption("-preset", "faster")
            .size("1280x720")
            .on("progress", function (progress) {
              fileBar.update(Number(progress.percent.toFixed(0)));
            })
            .on("end", () => {
              multi.remove(fileBar);
              resolve();
            })
            .on("error", (err) => {
              reject(err);
              //TODO
            })
            .run();
        });
        multi.remove(stepBar);
        // Supprimer le fichier mkv
        await unlink(filePath);
        fileNumber++;
      }
    }
  } catch (err) {
    console.error(err);
  } finally {
    if (localBar) {
      localBar.stop();
    }
    if (globalBar) {
      globalBar.update(2, { custom: "Extracting thumbnails" });
    }
  }
  localBar.stop();
  globalBar.update(2, { custom: "Extracting thumbnails" });
  // Extraire les miniatures
  await extractThumbnails();
}

async function extractThumbnails() {
  try {
    const files = await readdir(serieDir);
    let fileNumber = 0;
    for (const file of files) {
      const ext = path.extname(file);
      if (ext === ".mp4") {
        fileNumber++;
      }
    }
    localBar = multi.create(fileNumber, 0, { custom: "Starting..." });

    let fileCounter = 0;
    for (const file of files) {
      const filePath = path.join(serieDir, file);
      const ext = path.extname(file);
      const fileName = path.basename(file, ext);

      if (ext === ".mp4") {
        localBar.update(fileCounter, { custom: `Treating ${fileName}` });
        // Générer la miniature
        await new Promise((resolve, reject) => {
          ffmpeg(filePath)
            .on("end", () => {
              resolve();
              //TODO
            })
            .on("error", reject)
            .screenshots({
              timestamps: [600],
              filename: fileName + ".png",
              folder: serieDir,
              size: "320x180",
            });
        });
        fileCounter++;
      }
    }
  } catch (err) {
    console.error(err);
  } finally {
    if (localBar) {
      localBar.stop();
    }
    if (globalBar) {
      globalBar.stop();
    }
  }
  multi.stop();
  console.log(
    chalk.green("Subtitles and thumbnails extracted; conversion done!")
  );
}

extractSubtitlesAndThumbnails();
