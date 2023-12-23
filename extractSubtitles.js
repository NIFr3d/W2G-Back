const fs = require('fs');
const path = require('path');
const mkvExtract = require('./mkvExtract'); 

const videoDir = './videos'; 

fs.readdir(videoDir, (err, files) => {
  if (err) {
    console.error(`Error reading directory: ${err}`);
    return;
  }

  files.forEach(file => {
    const filePath = path.join(videoDir, file);
    const ext = path.extname(file);
    const fileName = path.basename(file, ext);

    if (ext === '.mkv') {
      const fileStream = fs.createReadStream(filePath, { highWaterMark: 2 * 1024 * 1024 }); // Taille de chunk de 2 Mo

      mkvExtract.mkvExtract(fileStream, (err, files) => {
        if (err) {
          console.error(`Error extracting subtitles: ${err}`);
          return;
        }

        files.forEach(f => {
          if (f.name.endsWith('.ass') || f.name.endsWith('.ssa')) {
            const outExt = f.name.endsWith('.ass') ? '.ass' : '.ssa';
            const outputPath = path.join(videoDir, fileName+outExt);
            fs.writeFile(outputPath, f.data, err => {
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
  });
});