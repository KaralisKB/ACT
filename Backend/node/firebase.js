const admin = require('firebase-admin');
const dotenv = require('dotenv');

dotenv.config();

if (!admin.apps.length) {
  const firebaseCredentials = JSON.parse(process.env.FIREBASE_CREDENTIALS);
  admin.initializeApp({
    credential: admin.credential.cert(firebaseCredentials),
  });
}

module.exports = admin;
