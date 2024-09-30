const express = require('express');
const admin = require('firebase-admin');
const dotenv = require('dotenv');

// load the enironment stuff
dotenv.config()

// Firebase Admin initialisation 
admin.initializeApp({
    credential: admin.credential.cert(require(process.env.FIREBASE_CREDENTIALS))
})

const db = admin.firestore();

const app = express();
app.use(express.json());
app.listen(3000, () => {
    console.log("Server is running on port 3000."); 
})
