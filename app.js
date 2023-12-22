const express = require('express');
const fs = require('fs');
const path = require('path');
const cors = require('cors');

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
app.listen(8080, () => console.log('Express lancé au port 8080'));



//Partie websocket
let paused = true;
let currentTime = "0";
  
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
        currentTime = message.currentTime;
        wss.clients.forEach(client => {
            if (client.readyState === WebSocket.OPEN && client != ws) {
                client.send(JSON.stringify({ event: 'setTime', currentTime: currentTime }));
            }
        });
    }
}
onError = (error) => {
    console.log(`Error occured: ${error}`);
}
onClose = () => {
    console.log('Client disconnected');
}