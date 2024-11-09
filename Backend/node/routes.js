const express = require('express');
const admin = require('./firebase');
const db = admin.firestore();
const stripe = require('stripe')('sk_test_51QIBRoCd7KzAIIn8iJCQqCRhs6UgIe2A2pn00m2ATgYVN3gxqdHoUJ22Iq3gncDE7Ng6WpguWICaTzwT5JSohwbF00hEiKjG6f');

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
            balance: 0
        });

        await db.collection('users').doc(userRecord.uid).set({
            email: email,
            userName: userName,
            role: role, //ie fund manager or admin.
            balance: 0,
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });

        res.status(201).send({
            message: "User has registered succesfully.",
            user: {
                uid: userRecord.uid,
                email: userRecord.email,
                userName: userRecord.userName,
                role: role,
                balance: balance
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

// Add Balance to user
router.post('/balance/add', async (res, req) => {
    const { clientID, moneyAmount } = req.body;

    try {
        const userRecord = await db.collection('users').doc(uid).get();

        if(!userDoc.exists) {
            return res.status(404).send({message: "User not found in database."})
        }

    } catch (error) {
        res.status(500).json({ error: error.message });
    }

});

const endpointSecret = "whsec_UyYuBiVfHRNWpwMRqLXTC8vAVHrFPbqR";

router.post('/webhook', (req, res) => {
    const sig = req.headers['stripe-signature'];

    let event;
    try {
        event = stripe.webhooks.constructEvent(req.body, sig, endpointSecret);
    } catch (err) {
        console.log("⚠  Webhook signature verification failed.");
        return res.sendStatus(400);
    }

    // Handle the event
    if (event.type === 'payment_intent.succeeded') {
        const paymentIntent = event.data.object;
        const userId = paymentIntent.metadata.userId; // Assuming you attach user ID in metadata
        const amount = paymentIntent.amount_received / 100; // Stripe amounts are in cents

        // Update Firebase balance
        const userRef = admin.firestore().collection('users').doc(uid);
        userRef.update({
            balance: admin.firestore.FieldValue.increment(amount)
        })
        .then(() => console.log("Balance updated for user ${userId}"))
        .catch(error => console.error("Error updating balance:", error));
    }

    res.json({received: true});
});




module.exports = router;