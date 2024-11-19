const express = require('express');
const admin = require('./firebase');
const bodyParser = require('body-parser');
const db = admin.firestore();
const stripe = require('stripe')('sk_test_51QIBRoCd7KzAIIn8iJCQqCRhs6UgIe2A2pn00m2ATgYVN3gxqdHoUJ22Iq3gncDE7Ng6WpguWICaTzwT5JSohwbF00hEiKjG6f'); // Replace with your Stripe secret key

const router = express.Router();

router.use(express.json());

// Firestore refs
const portfolioColletion = db.collection('portfolio');

router.post("/auth/register", async (req, res) => {
    const { uid, email, firstName, lastName, role } = req.body;

    // Validate incoming request
    if (!uid || !email || !firstName || !lastName) {
        console.error("Missing required fields:", { uid, email, firstName, lastName, role });
        return res.status(400).send({ error: "Missing required fields." });
    }

    try {
        // Log incoming request for debugging
        console.log("Registering user with details:", { uid, email, firstName, lastName, role });

        // Check if the user already exists in Firestore
        const userDoc = await db.collection("users").doc(uid).get();
        if (userDoc.exists) {
            console.error("User with this UID already exists in Firestore:", uid);
            return res.status(400).send({ error: "User already exists in Firestore." });
        }

        // Add user details to Firestore
        await db.collection("users").doc(uid).set({
            email,
            firstName,
            lastName,
            role: role || "admin", // Default role if not provided
            balance: 0,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        console.log("User successfully registered in Firestore:", uid);
        res.status(201).send({ message: "User registered successfully." });
    } catch (error) {
        // Log the error for debugging
        console.error("Error saving user to Firestore:", error);

        // Differentiate Firestore errors
        if (error.code === "permission-denied") {
            return res.status(403).send({ error: "Insufficient permissions to save user in Firestore." });
        }
    }
});

//Login

router.post('/auth/login', async (req, res) => {
    const { idToken } = req.body;

    if (!idToken) {
        return res.status(400).send({ error: "Missing idToken in request." });
    }

    try {
        // Verify the ID token using Firebase Admin SDK
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        const { uid, email } = decodedToken;

        // Fetch user data from Firestore
        const userDoc = await db.collection('users').doc(uid).get();
        if (!userDoc.exists) {
            return res.status(404).send({ error: "User not found in Firestore." });
        }

        const userData = userDoc.data();

        res.status(200).send({
            message: "Login successful",
            user: {
                id: uid,
                email: userData.email,
                firstName: userData.firstName,
                lastName: userData.lastName,
                role: userData.role,
            },
        });
    } catch (error) {
        console.error("Error verifying token:", error);
        res.status(401).send({ error: "Unauthorized" });
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
        const userDoc = userQuerySnapshot.docs[0]; 
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

const endpointSecret = 'whsec_ZzpwcZDTquTdVspM4lGfKSUrKMn0WbR5'; 


router.post('/webhook/', express.json({ type: 'application/json' }), async  (req, res) => {
    const event = req.body;

    // Handle the event
    switch (event.type) {
        case 'checkout.session.completed':
            const session = event.data.object;

            // Extract relevant details from the session
            const customerEmail = session.customer_details.email; // Customer's email
            const amountPaid = session.amount_total / 100; 
            const currency = session.currency; 

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

router.post('/watchlist/add', async (req, res) => {
    const { userId, stockTicker } = req.body;

    if (!userId || !stockTicker) {
        return res.status(400).send({ error: "Missing required fields." });
    }

    try {
        const userWatchlistRef = db.collection('users').doc(userId).collection('watchlist');
        await userWatchlistRef.doc(stockTicker).set({ ticker: stockTicker });
        res.status(201).send({ message: "Stock added to watchlist." });
    } catch (error) {
        console.error("Error adding stock to watchlist:", error);
        res.status(500).send({ error: "Failed to add stock to watchlist." });
    }
});

router.get('/watchlist/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        const userWatchlistRef = db.collection('users').doc(userId).collection('watchlist');
        const snapshot = await userWatchlistRef.get();
        const watchlist = snapshot.docs.map(doc => doc.data());
        res.status(200).send(watchlist);
    } catch (error) {
        console.error("Error fetching watchlist:", error);
        res.status(500).send({ error: "Failed to fetch watchlist." });
    }
});

router.delete('/watchlist/remove', async (req, res) => {
    const { userId, stockTicker } = req.body;

    if (!userId || !stockTicker) {
        return res.status(400).send({ error: "Missing required fields." });
    }

    try {
        const userWatchlistRef = db.collection('users').doc(userId).collection('watchlist');
        await userWatchlistRef.doc(stockTicker).delete();
        res.status(200).send({ message: "Stock removed from watchlist." });
    } catch (error) {
        console.error("Error removing stock from watchlist:", error);
        res.status(500).send({ error: "Failed to remove stock from watchlist." });
    }
});

router.get('/user/balance/:userId', async (req, res) => {
    const { userId } = req.params;

    if (!userId) {
        return res.status(400).json({ error: "User ID is required." });
    }

    try {
        // Fetch user document from Firestore
        const userDoc = await db.collection('users').doc(userId).get();

        if (!userDoc.exists) {
            return res.status(404).json({ error: "User not found." });
        }

        const userData = userDoc.data();
        const userBalance = userData.balance || 0; // Default to 0 if balance is not defined

        res.status(200).json({ balance: userBalance });
    } catch (error) {
        console.error("Error fetching user balance:", error);
        res.status(500).json({ error: "Failed to fetch user balance." });
    }
});

module.exports = router;