const express = require('express');
const admin = require('firebase-admin');
const dotenv = require('dotenv');
const routes = require("./routes");

// load the enironment stuff
dotenv.config()

// Firebase Admin initialisation 

const firebaseCredentials = JSON.parse(process.env.FIREBASE_CREDENTIALS)
admin.initializeApp({
    credential: admin.credential.cert(firebaseCredentials)
})

const db = admin.firestore();

const app = express();
app.use(express.json());

// access routes from routes.js
app.use('/api', routes);


app.listen(3000, () => {
    console.log("Server is running on port 3000."); 
})
