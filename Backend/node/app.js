const express = require('express');
const admin = require('./firebase');
const dotenv = require('dotenv');
const routes = require("./routes");

// load the enironment stuff
dotenv.config()

const db = admin.firestore();

const app = express();
app.use(express.json());

// access routes from routes.js
app.use('/api', routes);

app.listen(3000, () => {
    console.log("Server is running on port 3000."); 
})

