const express = require('express');
const admin = require('./firebase');
const db = admin.firestore();

const router = express.Router();

// Firestore refs
const portfolioColletion = db.collection('portfolio');

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

// Create a new portfolio 
router.post('/portfolios', async (res, req) => {
    const { clientID, value } = req.body;

    try {
        const portfolioData = {
            assets: [],
            value,
            lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
        };

        if (clientID) {
            portfolioData.clientID = clientID
        } 
        

        const newPortfolioAdd = portfolioColletion.doc();
        await newPortfolioAdd.set(portfolioData);

        res.status(201).json({ message: 'Portfolio created', portfolioID: newPortfolioAdd.id});
    }catch (error) {
        res.status(500).json({ error: error.message });
    }
});

router.get('/stocks', async (req, res) => {
    try {
        const stockRef = db.collection('stocks');
        const snapshot = await stockRef.get();
        if(snapshot.empty) {
            return res.status(404).json({ message: "No Stocks Found"});
        }

        const stocks = [];
        snapshot.forEach(doc => {
            stocks.push({ id: doc.id, ...doc.data()});
        });

        res.status(200).json(stocks);
    }catch (error) {
        console.error('Error retrieving stocks:', error);
        res.status(500).json({ message: 'Internal Server Error'});
    }
});

module.exports = router;