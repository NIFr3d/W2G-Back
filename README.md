# W2G-Back

Ceci est l'application backend qui sert les fichiers vidéos et sous-titres situés dans le sous-dossier `videos`. Elle lance aussi un websocket pour la synchronisation entre les utilisateurs regardant la même vidéo.

## Installation

1. Clonez ce dépôt sur votre machine locale.
2. Assurez-vous d'avoir Node.js installé sur votre machine.
3. Exécutez la commande `npm install` pour installer les dépendances.

## Génération des fichiers de sous-titres

Utilisez le script `extractSubtitles.js` avec Node.js pour générer les fichiers de sous-titres à partir de fichiers MKV.

Exécutez la commande suivante :
node extractSubtitles.js

## Lancement de l'application
node app.js
