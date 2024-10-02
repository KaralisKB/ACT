const express = require('express');
const admin = require('./firebase');
const db = admin.firestore();

const router = express.Router();

// Registration
router.post('/auth/register', async (req, res) => {
    const {email, password, userName, role} = req.body;

    try{
        const userRecord = await admin.auth().createUser({
            email: email,
            password: password,
            userName: userName,
        });

        await db.collection('users').doc(userRecord.uid).set({
            email: email,
            userName: userName,
            role: role, //ie fund manager or admin.
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        res.status(201).send({
            message: "User has registered succesfully.",
            user: {
                uid: userRecord.uid,
                email: userRecord.email,
                userName: userRecord.userName,
                role: role,
            }
        });
    } catch (error) {
        res.status(400).send({error: error.message});
    }
});

//Login

router.post('/auth/login', async (res, req) => {
    const {idToken} = req.body;

    try {
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        const uid = decodedToken.uid;

        const userRecord = await db.collection('users').doc(uid).get();

        if(!userDoc.exists) {
            return res.status(404).send({message: "User not found in database."})
        }

        const userData = userRecord.data();
        res.status(200).send({message: "Login succesful!", user: userData});

    } catch (error) {
        res.status(401).send({ error: "Unauthorized"});
    }
});

module.exports = router;