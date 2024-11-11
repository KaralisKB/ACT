const express = require('express');
const admin = require('./firebase');
const db = admin.firestore();
const paypal = require('@paypal/checkout-server-sdk')


const router = express.Router();

const environment = new paypal.core.SandboxEnvironment('ASheza9zVdnhKiMQic87pN_if978hmrnmHctvst10vBDnrjV5GFcSzqFbvDnMS-ZTU6Kxx1OaAws4Fp-', 'EE89AzAu7PGEdRcpXVwrIkuTMQ9cncuzQrl2pYjTbct_0I8kss2iXehCzAVaqDlKR_LpQ7uPruPlr_gT');
const client = new paypal.core.PayPalHttpClient(environment);


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

router.post('/create-order', async (req, res) => {
    const { amount } = req.body;

    const request = new paypal.orders.OrdersCreateRequest();
    request.requestBody({
        intent: 'CAPTURE',
        purchase_units: [{
            amount: {
                currency_code: 'USD',
                value: amount
            }
        }]
    });

    try {
        const order = await client.execute(request);
        res.json({ orderId: order.result.id });
    } catch (error) {
        res.status(500).send(error);
    }
    
});

router.post('/capture-order', async (req, res) => {
    const { orderId } = req.body;

    const request = new paypal.orders.OrdersCaptureRequest(orderId);
    request.requestBody({});

    try {
        const capture = await client.execute(request);
        
        // Payment succeeded, update user balance in the database
        const amount = capture.result.purchase_units[0].payments.captures[0].amount.value;
        
        // Assuming a function updateUserBalance to update the user's balance
        await updateUserBalance(req.user.id, parseFloat(amount));
        
        res.json({ status: 'Payment captured successfully' });
    } catch (error) {
        res.status(500).send(error);
    }
});

async function updateUserBalance(uid, amount) {
    try {
        const userRecord = await db.collection('users').doc(uid).get();

        if(!userDoc.exists) {
            return res.status(404).send({message: "User not found in database."})
        }

        const currentBalance = userDoc.data().balance || 0;

        const newBalance = currentBalance + amount;

        await userRecord.update({ balance: newBalance});

        console.log('Balance has updated succesfully.');

    } catch (error) {
        res.status(500).json({ error: error.message });
    }
}





module.exports = router;