# Base image
FROM node:14

# Set the working directory
WORKDIR /app


# Install npm dependencies
COPY package*.json ./
RUN npm install

# Expose ports
EXPOSE 8080
EXPOSE 8081

# Copiez le reste des fichiers de l'application dans le conteneur
COPY . .


# Set the volume for w2g folder
VOLUME /app/w2g


# Start the application
CMD ["node", "app.js"]