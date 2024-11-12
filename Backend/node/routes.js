const express = require('express');
const admin = require('./firebase');
const bodyParser = require('body-parser');
const db = admin.firestore();
const stripe = require('stripe')('sk_test_YOUR_SECRET_KEY'); // Replace with your Stripe secret key


const router = express.Router();

router.use(express.json());

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

router.post('/auth/login', async (req, res) => {
    const {idToken} = req.body;

    try {
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        const uid = decodedToken.uid;

        const userRecord = await db.collection('users').doc(uid).get();

        if(!userRecord.exists) {
            return res.status(404).send({message: "User not found in database."})
        }

        const userData = userRecord.data();
        res.status(200).send({message: "Login succesful!", user: userData});

    } catch (error) {
        res.status(401).send({ error: "Unauthorized"});
    }
});

// Create a new portfolio 
router.post('/portfolios', async (req, res) => {
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
router.post('/balance/add', async (req, res) => {
    const { clientID, moneyAmount } = req.body;

    try {
        const userRecord = await db.collection('users').doc(uid).get();

        if(!userRecord.exists) {
            return res.status(404).send({message: "User not found in database."})
        }

    } catch (error) {
        res.status(500).json({ error: error.message });
    }

});


async function updateUserBalance(email, amount) {
    try {
        // Find the user by email
        const userQuerySnapshot = await db.collection('users').where('email', '==', email).get();

        if (userQuerySnapshot.empty) {
            console.error(`User with email ${email} not found.`);
            return { success: false, message: "User not found in database." };
        }

        // Get the user document reference
        const userDoc = userQuerySnapshot.docs[0]; // Assuming email is unique
        const userRef = userDoc.ref;

        // Get current balance and update it
        const currentBalance = userDoc.data().balance || 0;
        const newBalance = currentBalance + amount;

        await userRef.update({ balance: newBalance });

        console.log(`Balance has been successfully updated for user with email ${email}. New Balance: ${newBalance}`);
        return { success: true };
    } catch (error) {
        console.error(`Error updating balance for user with email ${email}:`, error.message);
        throw error;
    }
}


router.use(bodyParser.json());

const endpointSecret = 'whsec_ZzpwcZDTquTdVspM4lGfKSUrKMn0WbR5'; // Replace with your webhook secret


router.post('/webhook/', express.json({ type: 'application/json' }), (req, res) => {
    const event = req.body;

    // Handle the event
    switch (event.type) {
        case 'checkout.session.completed':
            const session = event.data.object;

            // Extract relevant details from the session
            const customerEmail = session.customer_details.email; // Customer's email
            const amountPaid = session.amount_total / 100; // Total amount paid (in dollars, assuming USD)
            const currency = session.currency; // Currency (e.g., "usd")

            console.log(`Payment completed: ${amountPaid} ${currency} from ${customerEmail}`);

            try {
                // Update user balance using the email
                const result = await updateUserBalance(customerEmail, amountPaid);

                if (result.success) {
                    console.log(`Balance updated successfully for user with email ${customerEmail}`);
                } else {
                    console.error(result.message);
                }
            } catch (error) {
                console.error(`Failed to update balance for user with email ${customerEmail}:`, error.message);
            }

            break;

        default:
            console.log(`Unhandled event type: ${event.type}`);
    }

    // Acknowledge receipt of the event
    res.status(200).json({ received: true });
});



module.exports = router;