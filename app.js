const express = require('express');
const fs = require('fs');
const path = require('path');
const cors = require('cors');
const sqlite3 = require('sqlite3').verbose();
const {getVideoDurationInSeconds} = require('get-video-duration');
var { videoDir} = require('./config.js');
const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 8081 });
console.log('WebSocket lancé au port 8081')


//Partie DB
const dbPath = path.join(videoDir, 'db.sqlite');
const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('Error opening database:', err);
    } else {
        // Create the resume_watching table if it doesn't exist
        db.run(`
            CREATE TABLE IF NOT EXISTS resume_watching (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                serie TEXT,
                season INTEGER,
                episode INTEGER,
                currentTime INTEGER
            )
        `, (err) => {
            if (err) {
                console.error('Error creating resume_watching table:', err);
            } else {
                console.log('resume_watching table created');
            }
        });
    }
});

//Partie express
const app = express();
app.use(cors());
// const videoDir = path.join(__dirname, 'videos');



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

app.get('/series', (req, res) => {
    fs.readdir(videoDir, (err, files) => {
        if (err) {
            res.status(500).send('Error reading video directory');
        } else {
            files = files.filter(file => {
                return fs.lstatSync(path.join(videoDir, file)).isDirectory();
            });
            res.json(files);
        }
    });
});

function isSoloSeason(series) {
    return fs.readdirSync(path.join(videoDir, series)).every(file => {
        return (/^E\d{2}\.\w+$/.test(file) || file.toLowerCase() == 'thumbnail.jpg');
    });
}
function isMultiSeason(series) {
    return fs.readdirSync(path.join(videoDir, series)).every(file => {
        return (/^S\d{2}E\d{2}\.\w+$/.test(file) || file.toLowerCase() == 'thumbnail.jpg');
    });
}

app.get('/series/:serie/seasons', (req, res) => {
    const seriesName = req.params.serie;
    const seriesPath = path.join(videoDir, seriesName);
    fs.readdir(seriesPath, (err, files) => {
        if (err) {
            res.status(500).send('Error reading video directory');
        } else {
            const firstFile = files[0];
            if(isSoloSeason(seriesName)) {
                res.json(['01']);
            } else if(isMultiSeason(seriesName)) {
                const seasons = files.map(file => file.split('E')[0]).filter(file => file.toLowerCase() !== 'thumbnail.jpg');
                const uniqueSeasons = [...new Set(seasons.map(season => season.replace('S', '')))];
                res.json(uniqueSeasons);
            } else {
                res.status(500).send('Wrong file format in videos directory');
            }
        }
    });
});

app.get('/series/:serie/:season/episodes', (req, res) => {
    const seriesName = req.params.serie;
    let season = req.params.season;
    if(season < 10) season = '0' + season;
    const seriesPath = path.join(videoDir, seriesName);
    fs.readdir(seriesPath, (err, files) => {
        if (err) {
            res.status(500).send('Error reading video directory');
        } else {
            if(isSoloSeason(seriesName) && (season === '01' || season === '1')) {
                const episodes = [...new Set(files.filter(file => file.startsWith(`E`)).map(file => file.split('E')[1].split('.')[0]))];
                res.json(episodes);
            } else if(isMultiSeason(seriesName)) {
                const episodes = [...new Set(files.filter(file => file.startsWith(`S${season}`)).map(file => file.split('E')[1].split('.')[0]))];
                res.json(episodes);
            } else {
                res.status(500).send('Wrong file format in videos directory');
            }
        }
    });
});

app.get('/episode/:serie/:season/:episode', (req, res) => {
    const serie = req.params.serie;
    const season = req.params.season;
    const episode = req.params.episode;
    let episodePathMkv;
    let episodePathMp4;
    if(isSoloSeason(serie)) {
        episodePathMkv = path.join(videoDir, serie, `E${episode}.mkv`);
        episodePathMp4 = path.join(videoDir, serie, `E${episode}.mp4`);
        // si nécessaire, on peut ajouter d'autres formats ici
    } else if(isMultiSeason(serie)) {
        episodePathMkv = path.join(videoDir, serie, `S${season}E${episode}.mkv`);
        episodePathMp4 = path.join(videoDir, serie, `S${season}E${episode}.mp4`);
        // si nécessaire, on peut ajouter d'autres formats ici
    } else {
        res.status(500).send('Wrong file format in videos directory');
    }
    fs.access(episodePathMkv, fs.constants.F_OK, (err) => {
        if (err) {
            fs.access(episodePathMp4, fs.constants.F_OK, (err) => {
                if (err) {
                    res.status(404).send('File not found');
                } else {
                    res.sendFile(episodePathMp4);
                }
            });
        } else {
            res.sendFile(episodePathMkv);
        }
    });
});

app.get('/episode/:serie/:season/:episode/subtitles', (req, res) => {
    const serie = req.params.serie;
    const season = req.params.season;
    const episode = req.params.episode;
    let episodePathAss;
    let episodePathSsa;
    if(isSoloSeason(serie)) {
        episodePathAss = path.join(videoDir, serie, `E${episode}.ass`);
        episodePathSsa = path.join(videoDir, serie, `E${episode}.ssa`);
        // si nécessaire, on peut ajouter d'autres formats ici
    } else if(isMultiSeason(serie)) {
        episodePathAss = path.join(videoDir, serie, `S${season}E${episode}.ass`);
        episodePathSsa = path.join(videoDir, serie, `S${season}E${episode}.ssa`);
    } else {
        res.status(500).send('Wrong file format in videos directory');
    }
    fs.access(episodePathAss, fs.constants.F_OK, (err) => {
        if (err) {
            fs.access(episodePathSsa, fs.constants.F_OK, (err) => {
                if (err) {
                    res.status(404).send('File not found');
                } else {
                    res.sendFile(episodePathSsa);
                }
            });
        } else {
            res.sendFile(episodePathAss);
        }
    });   
});

function formatEpisode(episode) {
    let season;
    let episodeNumber;

    if (episode.startsWith("E")) {
        season = 1;
        episodeNumber = parseInt(episode.slice(1));
    } else if (episode.startsWith("S")) {
        const parts = episode.slice(1).split("E");
        season = parseInt(parts[0]);
        episodeNumber = parseInt(parts[1]);
    }

    return { season, episode: episodeNumber };
}

app.get('/nextEpisode/:serie/:season/:episode', (req, res) => {
    const serie = req.params.serie;
    const season = req.params.season;
    const episode = req.params.episode;
    let episodeplus1 = parseInt(episode) + 1;
    if(episodeplus1 < 10) episodeplus1 = '0' + episodeplus1;
    let nextEpisode;
    let nextEpisode2;
    if(isSoloSeason(serie)) {
        nextEpisode = `E${episodeplus1}`;
    } else if(isMultiSeason(serie)) {
        let seasonNbr = parseInt(season);
        let seasonNbrplus1 = seasonNbr + 1;
        if(seasonNbr < 10) seasonNbr = '0' + seasonNbr;
        if(seasonNbrplus1 < 10) seasonNbrplus1 = '0' + seasonNbrplus1;
        nextEpisode = `S${season}E${episodeplus1}`;
        nextEpisode2 = `S${seasonNbrplus1}E01`;
    } else {
        res.status(500).send('Wrong file format in videos directory');
    }
    const seriePath = path.join(videoDir, serie);
    fs.readdir(seriePath, (err, files) => {
        if (err) {
            res.status(500).send('Error reading video directory');
        } else {
            if(files.includes(`${nextEpisode}.mkv`) || files.includes(`${nextEpisode}.mp4`)) {
                res.json(formatEpisode(nextEpisode));
            } else if(files.includes(`${nextEpisode2}.mkv`) || files.includes(`${nextEpisode2}.mp4`)) {
                res.json(formatEpisode(nextEpisode2));
            } else {
                res.status(404).send('Current episode is the last one');
            }
        }
    });
});

app.get('/thumbnail/:serie', (req, res) => {
    const serie = req.params.serie;
    const seriePath = path.join(videoDir, serie);
    fs.readdir(seriePath, (err, files) => {
        if (err) {
            res.status(500).send('Error reading video directory');
        } else {
            const thumbnail = files.find(file => file.toLowerCase().endsWith('.jpg'));
            res.sendFile(path.join(seriePath, thumbnail));
        }
    });
});

app.get('/search/:query', (req, res) => {
    const query = req.params.query;
    fs.readdir(videoDir, (err, files) => {
        if (err) {
            res.status(500).send('Error reading video directory');
        } else {
            files = files.filter(file => {
                return fs.lstatSync(path.join(videoDir, file)).isDirectory() && file.toLowerCase().includes(query.toLowerCase());
            });
            res.json(files);
        }
    });
});

app.get('/resumeWatching', (req, res) => {
    db.all(`
        SELECT * FROM resume_watching
    `, [], (err, rows) => {
        if (err) {
            console.error('Error selecting from resume_watching:', err);
            res.status(500).send('Error selecting from resume_watching');
        } else {
            const promises = rows.map(row => {
                const currentTime = row.currentTime;
                let videoPath;
                if(isSoloSeason(row.serie)) {
                    row.episode < 10 ? row.episode = '0' + row.episode : row.episode;
                    videoPath = path.join(videoDir, row.serie, `E${row.episode}.mp4`);
                } else if(isMultiSeason(row.serie)) {
                    row.season < 10 ? row.season = '0' + row.season : row.season;
                    row.episode < 10 ? row.episode = '0' + row.episode : row.episode;
                    videoPath = path.join(videoDir, row.serie, `S${row.season}E${row.episode}.mp4`);
                }

                return getVideoDurationInSeconds(videoPath).then(duration => {
                    const totalVideoTime = duration;
                    const progression = (currentTime / totalVideoTime) * 100;
                    return { ...row, progression };
                });
            });

            // Attendez que toutes les promesses soient résolues avant d'envoyer la réponse
            Promise.all(promises).then(rowsWithProgression => {
                res.json(rowsWithProgression);
            }).catch(err => {
                console.error('Error getting video durations:', err);
                res.status(500).send('Error getting video durations');
            });
        }
    });
});

app.get('/otherSeries', (req, res) => {
    fs.readdir(videoDir, (err, files) => {
        if (err) {
            res.status(500).send('Error reading video directory');
        } else {
            files = files.filter(file => {
                return fs.lstatSync(path.join(videoDir, file)).isDirectory();
            });
            files = files.slice(0, 5);
            res.json(files);
        }
    });
});

app.listen(8080, () => console.log('Express lancé au port 8080'));




//Partie websocket

const rooms = new Map();

wss.on('connection', ws => {
    onConnection(ws);
    ws.on('message', message => {
        onMessage(message, ws);
    });
    ws.on('error', error => {
        onError(error);
    });
    ws.on('close', () => {
        onClose(ws);
    })
});


onConnection = (ws) => {
    console.log('Client connected');
    ws.send(JSON.stringify({ event: 'welcome'}));
}
onMessage = (message, ws) => {
    console.log(`Received message => ${message}`);
    message = JSON.parse(JSON.parse(message));
    if (message.event == "joinRoom") {
        if(!rooms.has(message.room)) {
            rooms.set(message.room, { clients: new Set(), paused: true, currentTime: 0 });
        }
        rooms.get(message.room).clients.add(ws);
        ws.send(JSON.stringify({ event: 'joinedRoom', currentTime: rooms.get(message.room).currentTime, paused: rooms.get(message.room).paused }));
    }
    if (message.event == "pause") {
        rooms.forEach(room => {
            if (room.clients.has(ws)) {
                room.paused = true;
                room.currentTime = message.currentTime;
                room.clients.forEach(client => {
                    if (client.readyState === WebSocket.OPEN && client !== ws) {
                        client.send(JSON.stringify({ event: 'pause', currentTime: room.currentTime }));
                    }
                });
            }
        });
    } else if (message.event == "play") {
        rooms.forEach(room => {
            if (room.clients.has(ws)) {
                room.paused = false;
                room.clients.forEach(client => {
                    if (client.readyState === WebSocket.OPEN && client !== ws) {
                        client.send(JSON.stringify({ event: 'play' }));
                    }
                });
            }
        });
    }
    else if (message.event == "setTime") {
        rooms.forEach(room => {
            if (room.clients.has(ws)) {
                if(Math.abs(room.currentTime - message.currentTime) > 1) {
                    room.currentTime = message.currentTime;
                    room.clients.forEach(client => {
                        if (client.readyState === WebSocket.OPEN && client !== ws) {
                            client.send(JSON.stringify({ event: 'setTime', currentTime: room.currentTime }));
                        }
                    });
                }
            }
        });
    }
    else if(message.event == "updateResumeWatching") {
        let serie = message.serie;
        let season = message.season;
        let episode = message.episode;
        let currentTime = message.currentTime;
        db.get(`
            SELECT * FROM resume_watching WHERE serie = ? ORDER BY id DESC LIMIT 1
        `, [serie], (err, row) => {
            if (err) {
            console.error('Error selecting from resume_watching:', err);
            } else {
            if (row) {
                db.run(`
                UPDATE resume_watching SET season = ?, episode = ?, currentTime = ? WHERE id = ?
                `, [season, episode, currentTime, row.id], (err) => {
                if (err) {
                    console.error('Error updating resume_watching:', err);
                } else {
                    console.log('Resume watching updated successfully');
                }
                });
            } else {
                db.run(`
                INSERT INTO resume_watching (serie, season, episode, currentTime)
                VALUES (?, ?, ?, ?)
                `, [serie, season, episode, currentTime], (err) => {
                if (err) {
                    console.error('Error inserting into resume_watching:', err);
                } else {
                    console.log('Resume watching inserted successfully');
                }
                });
            }
            }
        });
    }
}
onError = (error) => {
    console.log(`Error occured: ${error}`);
}
onClose = (ws) => {
    const roomKeys = Array.from(rooms.keys());
    roomKeys.forEach(roomKey => {
        const room = rooms.get(roomKey);
        if (room.clients.has(ws)) {
            room.clients.delete(ws);
            if(room.clients.size == 0) {
                // on utilise l'identifiant de la room pour la supprimer
                rooms.delete(roomKey);
            }
        }
    });
    console.dir(rooms);
    console.log('Client disconnected');
}