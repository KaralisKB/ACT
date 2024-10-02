const admin = require('firebase-admin');
const dotenv = require('dotenv');

// Load environment variables
dotenv.config();

// Initialize Firebase only if it hasn't been initialized yet
if (!admin.apps.length) {
  const firebaseCredentials = JSON.parse(process.env.FIREBASE_CREDENTIALS);
  admin.initializeApp({
    credential: admin.credential.cert(firebaseCredentials),
  });
}

module.exports = admin;
