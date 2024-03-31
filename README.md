# W2G-Back

Ceci est l'application backend qui sert des fichiers vidéos et sous-titres. Elle lance aussi un websocket pour la synchronisation entre les utilisateurs regardant la même vidéo.
Lien de l'application frontend à utiliser : https://github.com/NIFr3d/W2G

## Installation

1. Clonez ce dépôt sur votre machine locale.
2. Assurez-vous d'avoir Node.js installé sur votre machine.
3. Exécutez la commande `npm install` pour installer les dépendances.

## Format des fichiers

Vous devez renseigner le dossier qui contient les vidéos dans `config.js` à créer dans la racine. Exemple de fichier :

```javascript
const path = require("path");
videoDir = path.join(__dirname, "videos");
module.exports = {
  videoDir: videoDir, //videoDir est utilisé dans app.js, possible de le modifier
  videoDirNative: videoDir, //videoDirNative est utilisé pour le script extractSubtitlesThumbnails.js
};
```

Les fichiers doivent être mis dans le dossier videos.

Un sous-dossier par série qui porte le nom de la série.

Dans ce dossier, les épisodes doivent être du type SXXEXX ou juste EXX (XX = numéro de saison ou d'épisode).

Conversion des vidéos au format mp4 recommandée.

Vous pouvez ajouter une miniature par série, nommée thumbnail.jpg.

## Génération des fichiers de sous-titres

Utilisez le script `extractSubtitlesThumbnails.js` avec Node.js pour générer les fichiers de sous-titres à partir de fichiers MKV ainsi que les miniatures des épisodes à partir de fichiers MKV ou MP4.

Exécutez la commande suivante :
node extractSubtitlesThumbnails.js

## Lancement de l'application

node app.js

### Docker

Il est possible de lancer l'app dans un conteneur. Faites attention à monter un volume correctement pour pouvoir avoir accès aux fichiers vidéos dans le conteneur et modifier le fichier config.js en conséquence.
