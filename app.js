const express = require('express');
const fs = require('fs');
const path = require('path');
const from2 = require('from2');
const cors = require('cors');
const mkvExtract = require('./mkvExtract.js');

const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 8081 });
console.log('WebSocket lancé au port 8081')

//Partie express
const app = express();
app.use(cors());
const videoDir = path.join(__dirname, 'videos');

app.get('/videos', (req, res) => {
  fs.readdir(videoDir, (err, files) => {
    if (err) {
      res.status(500).send('Error reading video directory');
    } else {
        files = files.filter(file => {
            const ext = path.extname(file);
            return ext === '.mkv' || ext === '.mp4' || ext === '.avi';
        });
      res.json(files);
    }
  });
});
app.get('/videos/:filename', (req, res) => {
    const filename = req.params.filename;
    console.log(`Streaming video: ${filename}`);
    const filePath = path.join(videoDir, filename);

    fs.access(filePath, fs.constants.F_OK, (err) => {
        if (err) {
            console.log(`${filePath} ${err}`);
            res.status(404).send('File not found');
        } else {
            res.sendFile(filePath);
        }
    });
});
app.get('/subtitles/:filename', (req, res) =>{
    const filename = req.params.filename;
    console.log(`Asking subtitles for: ${filename}`);
    const filePath = path.join(videoDir, filename);

    fs.access(filePath, fs.constants.F_OK, (err) => {
        if (err) {
            console.log(`${filePath} ${err}`);
            res.status(404).send('File not found');
        } else {
            const ext = path.extname(filename);
            const filenameWithoutExt = path.basename(filename, ext);
            if (ext == ".mkv" || ext == ".mp4") {
                const subtitlesPathAss = path.join(videoDir, `${filenameWithoutExt}.ass`);
                const subtitlesPathSsa = path.join(videoDir, `${filenameWithoutExt}.ssa`);
                const subtitlesExistAss = fs.existsSync(subtitlesPathAss);
                const subtitlesExistSsa = fs.existsSync(subtitlesPathSsa);
                if (subtitlesExistAss) {
                    console.log(`Subtitles file exists: ${subtitlesPathAss}`);
                    res.sendFile(subtitlesPathAss);
                } else if (subtitlesExistSsa) {
                    console.log(`Subtitles file exists: ${subtitlesPathSsa}`);
                    res.sendFile(subtitlesPathSsa);
                } else {
                    console.log(`Subtitles file does not exist: ${subtitlesPathAss} or ${subtitlesPathSsa}`);
                }
            }
        }
    });
});
app.listen(8080, () => console.log('Express lancé au port 8080'));



//Partie websocket
let paused = true;
let currentTime = 0;
let lastMessageTime = Date.now();

wss.on('connection', ws => {
    onConnection(ws);
    ws.on('message', message => {
        let currentDate = Date.now();
        if (currentTime - lastMessageTime >= 500) {
            onMessage(message, ws);
            lastMessageTime = currentDate;
        }
    });
    ws.on('error', error => {
        onError(error);
    });
    ws.on('close', ws => {
        onClose();
    })
});
wss.on('connection', ws => {
    onConnection(ws);
    ws.on('message', message => {
        onMessage(message, ws);
    });
    ws.on('error', error => {
        onError(error);
    });
    ws.on('close', ws => {
        onClose();
    })
});

onConnection = (ws) => {
    console.log('Client connected');
    ws.send(JSON.stringify({ event: 'welcome', paused: paused, currentTime: currentTime}));
}
onMessage = (message, ws) => {
    console.log(`Received message => ${message}`);
    message = JSON.parse(JSON.parse(message));
    if (message.event == "pause") {
        paused = true;
        currentTime = message.currentTime;
        wss.clients.forEach(client => {
            if (client.readyState === WebSocket.OPEN && client != ws) {
                client.send(JSON.stringify({ event: 'pause', currentTime: currentTime }));
            }
        });
    } else if (message.event == "play") {
        paused = false;
        wss.clients.forEach(client => {
            if (client.readyState === WebSocket.OPEN && client != ws) {
                client.send(JSON.stringify({ event: 'play' }));
            }
        });
    }
    else if (message.event == "setTime") {
        if(Math.abs(message.currentTime - currentTime) > 1) {
            currentTime = message.currentTime;
            wss.clients.forEach(client => {
                if (client.readyState === WebSocket.OPEN && client != ws) {
                    client.send(JSON.stringify({ event: 'setTime', currentTime: currentTime }));
                }
            });
        }
    }
}
onError = (error) => {
    console.log(`Error occured: ${error}`);
}
onClose = () => {
    console.log('Client disconnected');
}