# W2G-Back

Ceci est l'application backend qui sert les fichiers vidéos et sous-titres situés dans le sous-dossier `videos`. Elle lance aussi un websocket pour la synchronisation entre les utilisateurs regardant la même vidéo.
Lien de l'application frendend à utiliser : https://github.com/NIFr3d/W2G

## Installation

1. Clonez ce dépôt sur votre machine locale.
2. Assurez-vous d'avoir Node.js installé sur votre machine.
3. Exécutez la commande `npm install` pour installer les dépendances.

## Format des fichiers

Les fichiers doivent être mis dans le dossier videos.

Un sous-dossier par série qui porte le nom de la série.

Dans ce dossier, les épisodes doivent être du type SXXEXX ou juste EXX (XX = numéro de saison ou d'épisode).

Conversion des vidéos au format mp4 recommandée.

Vous pouvez ajouter une miniature par série, nommée thumbnail.jpg.

## Génération des fichiers de sous-titres

Utilisez le script `extractSubtitles.js` avec Node.js pour générer les fichiers de sous-titres à partir de fichiers MKV.

Exécutez la commande suivante :
node extractSubtitles.js

## Lancement de l'application
node app.js
