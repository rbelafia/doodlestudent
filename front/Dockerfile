FROM node:10-alpine as build
WORKDIR /app
COPY package.json /app
RUN npm install
COPY . /app
RUN npm run build --prod --outputPath=./dist/
