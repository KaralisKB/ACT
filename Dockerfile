# Dockerfile for Node.JS backend

# Use official Node.JS image
FROM node:16

WORKDIR /app

COPY Backend/node/ /app/

RUN npm install

EXPOSE 3000

CMD ["npm", "start"]