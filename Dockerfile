# Base image
FROM node:14

# Set the working directory
WORKDIR /app


# Install npm dependencies
COPY package*.json ./
RUN npm i --only=production

# Expose ports
EXPOSE 8080
EXPOSE 8081

# Copiez le reste des fichiers de l'application dans le conteneur
COPY . .

# Start the application
CMD ["node", "app.js"]