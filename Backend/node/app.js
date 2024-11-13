const express = require('express');
const admin = require('./firebase');
const dotenv = require('dotenv');
const routes = require("./routes");

const cors = require('cors');


// load the enironment stuff
dotenv.config()

const db = admin.firestore();

const app = express();


app.use(cors({
    origin: 'https://actproject.netlify.app',
    methods: 'GET, POST, PUT, DELETE',
    allowedHeaders: 'Content-Type, Authorization',
    credentials: true,
  }));


app.use((req, res, next) => {
    if (req.originalUrl === '/api/webhook/') {
        // Do not parse the body for Stripe webhook
        next();
    } else {
        express.json()(req, res, next);
    }
});
// access routes from routes.js
app.use('/api', routes);

app.listen(3000, () => {
    console.log("Server is running on port 3000."); 
})



