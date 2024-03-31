const fs = require("fs");
const path = require("path");
const mkvExtract = require("./mkvExtract.js");
const ffmpeg = require("fluent-ffmpeg");

var { videoDirNative } = require("./config.js");
let serieToExtract;
let serieDir;

// récupérer l'argument passé en ligne de commande
if (process.argv.length > 2) {
  serieToExtract = process.argv[2];
  serieDir = path.join(videoDirNative, serieToExtract);
} else {
  console.log("No serie specified");
  return;
}

fs.readdir(serieDir, (err, files) => {
  if (err) {
    console.error(`Error reading directory: ${err}`);
    return;
  }

  files.forEach((file) => {
    const filePath = path.join(serieDir, file);
    const ext = path.extname(file);
    const fileName = path.basename(file, ext);

    if (ext === ".mkv") {
      // Si le fichier est un mkv, on extrait les sous-titres
      const fileStream = fs.createReadStream(filePath, {
        highWaterMark: 2 * 1024 * 1024,
      }); // Taille de chunk de 2 Mo

      mkvExtract.mkvExtract(fileStream, (err, files) => {
        if (err) {
          console.error(`Error extracting subtitles: ${err}`);
          return;
        }

        files.forEach((f) => {
          if (f.name.endsWith(".ass") || f.name.endsWith(".ssa")) {
            const outExt = f.name.endsWith(".ass") ? ".ass" : ".ssa";
            const outputPath = path.join(serieDir, fileName + outExt);
            fs.writeFile(outputPath, f.data, (err) => {
              if (err) {
                console.error(`Error writing file: ${err}`);
              } else {
                console.log(`File saved: ${outputPath}`);
              }
            });
          }
        });
      });
    }
    if (ext === ".mkv" || ext === ".mp4") {
      ffmpeg(filePath)
        .on("end", function () {
          console.log(`Thumbnail created for ${fileName}`);
        })
        .on("error", function (err) {
          console.error("Error generating thumbnail", err);
        })
        .screenshots({
          timestamps: ["00:10:00"], // Prend une capture d'écran à 10 minutes
          filename: fileName,
          folder: serieDir,
          size: "320x180", // Taille de la miniature
        });
    }
  });
});
