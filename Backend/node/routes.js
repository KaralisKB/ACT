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


async function updateClientBalance(userId, clientName, amount) {
  try {
      if (!userId || !clientName) {
          console.error("User ID or Client Name is missing.");
          return { success: false, message: "User ID and Client Name are required to update the client's balance." };
      }

      // Get the specific client's subcollection under the given user
      const clientQuerySnapshot = await db
          .collection('users')
          .doc(userId) // Use the provided userId instead of localStorage
          .collection('Clients')
          .where('name', '==', clientName)
          .get();

      if (clientQuerySnapshot.empty) {
          console.error(`Client with name "${clientName}" not found for user: ${userId}.`);
          return { success: false, message: "Client not found in database." };
      }

      // Get the client document reference
      const clientDoc = clientQuerySnapshot.docs[0];
      const clientRef = clientDoc.ref;

      // Get current balance and update it
      const currentBalance = clientDoc.data().balance || 0;
      const newBalance = currentBalance + amount;

      await clientRef.update({ balance: newBalance });

      console.log(`Balance updated successfully for client: ${clientName}. New Balance: ${newBalance}`);
      return { success: true };
  } catch (error) {
      console.error(`Error updating balance for client: ${clientName}`, error.message);
      throw error;
  }
}

router.use(bodyParser.json());

const endpointSecret = 'whsec_ZzpwcZDTquTdVspM4lGfKSUrKMn0WbR5';

router.post('/webhook', express.json({ type: 'application/json' }), async (req, res) => { 
  const event = req.body;

  try {
    switch (event.type) {
      case 'checkout.session.completed': {
        const session = event.data.object;

        // Extract email and payment amount
        const customerEmail = session.customer_details.email; // Email entered in payment form
        const amountPaid = session.amount_total / 100; // Stripe sends amounts in cents

        // Extract client name from custom_fields by matching the label
        let clientName = null;
        if (session.custom_fields && Array.isArray(session.custom_fields)) {
          const clientField = session.custom_fields.find(field => field.label?.custom === 'Client Name');
          clientName = clientField?.text?.value;
        }

        console.log(`Payment completed: ${amountPaid} from ${customerEmail} for client ${clientName}`);

        if (!clientName) {
          console.error('Client name is missing in custom fields.');
          return res.status(400).json({ error: 'Client name is missing in custom fields.' });
        }

        // Step 1: Find the user by email
        const userQuerySnapshot = await db.collection('users').where('email', '==', customerEmail).get();

        if (userQuerySnapshot.empty) {
          console.error(`User with email ${customerEmail} not found.`);
          return res.status(404).json({ error: 'User not found in database.' });
        }

        // Step 2: Get the user's client list
        const userDoc = userQuerySnapshot.docs[0];
        const userId = userDoc.id;
        const clientsRef = db.collection('users').doc(userId).collection('Clients');
        const clientsSnapshot = await clientsRef.get();

        if (clientsSnapshot.empty) {
          console.error(`No clients found for user with email ${customerEmail}.`);
          return res.status(404).json({ error: 'No clients found for user.' });
        }

        // Step 3: Find the matching client
        let matchedClient = null;
        clientsSnapshot.forEach((doc) => {
          const client = doc.data();
          if (client.name.toLowerCase() === clientName.toLowerCase()) {
            matchedClient = { id: doc.id, ...client };
          }
        });

        if (!matchedClient) {
          console.error(`Client with name ${clientName} not found for user ${customerEmail}.`);
          return res.status(404).json({ error: `Client ${clientName} not found for user.` });
        }

        // Step 4: Update the client's balance
        const clientRef = clientsRef.doc(matchedClient.id);
        const currentBalance = matchedClient.balance || 0;
        const newBalance = currentBalance + amountPaid;

        await clientRef.update({ balance: newBalance });

        console.log(`Balance updated successfully for client ${clientName}. New Balance: ${newBalance}`);

        res.status(200).json({ message: 'Balance updated successfully.' });
        break;
      }

      default:
        console.log(`Unhandled event type: ${event.type}`);
        res.status(400).end();
    }
  } catch (error) {
    console.error('Error processing webhook:', error.message);
    res.status(500).json({ error: 'Webhook processing failed.' });
  }
});


router.post('/watchlist/add', async (req, res) => {
  const { userId, stockTicker, clientId } = req.body;

  if (!userId || !stockTicker || !clientId) {
      return res.status(400).send({ error: "Missing required fields (userId, stockTicker, or clientId)." });
  }

  try {
      // Reference the client's watchlist subcollection
      const clientWatchlistRef = db
          .collection('users')
          .doc(userId)
          .collection('Clients')
          .doc(clientId)
          .collection('watchlist');

      // Add the stock to the watchlist subcollection
      await clientWatchlistRef.doc(stockTicker).set({ ticker: stockTicker });

      res.status(201).send({ message: "Stock added to the client's watchlist." });
  } catch (error) {
      console.error("Error adding stock to client's watchlist:", error);
      res.status(500).send({ error: "Failed to add stock to client's watchlist." });
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

router.get('/client/balance/:userId/:clientId', async (req, res) => {
    const { userId, clientId } = req.params;

    if (!userId || !clientId) {
        return res.status(400).json({ error: "User ID and Client ID are required." });
    }

    try {
        // Fetch client document from Firestore
        const clientDoc = await db
            .collection('users')
            .doc(userId)
            .collection('Clients')
            .doc(clientId)
            .get();

        if (!clientDoc.exists) {
            return res.status(404).json({ error: "Client not found." });
        }

        const clientData = clientDoc.data();
        const clientBalance = clientData.balance || 0; // Default to 0 if balance is not defined

        res.status(200).json({ balance: clientBalance });
    } catch (error) {
        console.error("Error fetching client balance:", error);
        res.status(500).json({ error: "Failed to fetch client balance." });
    }
});

router.post("/buy", async (req, res) => {
    const { userId, symbol, name, quantity, price, totalCost } = req.body;
  
    // Check if required fields are provided
    if (!userId || !symbol || !quantity || !price || !totalCost) {
      return res.status(400).json({ error: "Missing required fields in payload." });
    }
  
    const db = admin.firestore();
  
    try {
      // Get the user document from Firestore
      const userRef = db.collection("users").doc(userId);
      const userSnapshot = await userRef.get();
  
      if (!userSnapshot.exists) {
        return res.status(404).json({ error: "User not found." });
      }
  
      const userData = userSnapshot.data();
  
      // Check if user has enough balance
      if (userData.balance < totalCost) {
        return res.status(400).json({ error: "Insufficient balance." });
      }
  
      // Deduct the total cost from the user's balance
      const newBalance = userData.balance - totalCost;
      await userRef.update({ balance: newBalance });
  
      // Add the transaction to the user's "Transactions" subcollection
      const transactionData = {
        symbol,
        name,
        quantity,
        price,
        totalCost,
        type: "BUY", // Specify the type of transaction
        date: admin.firestore.FieldValue.serverTimestamp(),
      };
  
      await db.collection("users").doc(userId).collection("Transactions").add(transactionData);
  
      // Add the stock to the user's "Portfolio" subcollection
      const portfolioRef = db.collection("users").doc(userId).collection("Portfolio").doc(symbol);
      const portfolioSnapshot = await portfolioRef.get();
  
      if (portfolioSnapshot.exists) {
        // If the stock already exists in the portfolio, update its quantity
        const existingData = portfolioSnapshot.data();
        const newQuantity = existingData.quantity + quantity;
  
        await portfolioRef.update({
          quantity: newQuantity,
          averagePrice: ((existingData.quantity * existingData.averagePrice) + (quantity * price)) / newQuantity,
        });
      } else {
        // If the stock does not exist, add it as a new entry
        const portfolioData = {
          name,
          symbol,
          quantity,
          averagePrice: price,
        };
  
        await portfolioRef.set(portfolioData);
      }
  
      // Respond with success
      res.status(200).json({
        message: "Stock purchase successful.",
        newBalance,
        portfolio: {
          symbol,
          quantity,
          averagePrice: price,
        },
      });
    } catch (error) {
      console.error("Error processing buy transaction:", error);
      res.status(500).json({ error: "Failed to process the buy transaction." });
    }
  });

  // Sell Stock Endpoint
  router.post('/sell', async (req, res) => {
    const { userId, symbol, name, quantity, price, totalEarnings } = req.body;

    if (!userId || !symbol || !quantity || !price || !totalEarnings) {
        return res.status(400).json({ error: 'Missing required fields.' });
    }

    try {
        const userRef = db.collection('users').doc(userId);
        const stockRef = userRef.collection('Portfolio').doc(symbol);

        // Fetch user's stock holdings
        const stockDoc = await stockRef.get();
        if (!stockDoc.exists) {
            return res.status(404).json({ error: 'Stock not found in Portfolio.' });
        }

        const currentStock = stockDoc.data();

        // Validate if the user has enough stock to sell
        if (currentStock.quantity < quantity) {
            return res.status(400).json({ error: 'Insufficient quantity to sell.' });
        }

        // Add sell transaction
        await userRef.collection('Transactions').add({
            type: 'SELL',
            symbol,
            name,
            quantity,
            price,
            totalEarnings,
            date: admin.firestore.Timestamp.now(),
        });

        // Update holdings or delete stock if fully sold
        if (currentStock.quantity === quantity) {
            await stockRef.delete();
        } else {
            await stockRef.update({
                quantity: admin.firestore.FieldValue.increment(-quantity),
                totalCost: admin.firestore.FieldValue.increment(-totalEarnings),
            });
        }

        // Fetch the user's current balance
        const userDoc = await userRef.get();
        if (!userDoc.exists) {
            return res.status(404).json({ error: 'User not found.' });
        }

        const userData = userDoc.data();
        const updatedBalance = (userData.balance || 0) + totalEarnings;

        // Update the user's balance
        await userRef.update({
            balance: updatedBalance,
        });

        return res.json({ message: 'Stock sold successfully.', newBalance: updatedBalance });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ error: 'Internal Server Error' });
    }
});
  

  router.get('/portfolio/:userId', async (req, res) => {
    const { userId } = req.params;
  
    try {
      const db = admin.firestore();
      const portfolioRef = db.collection('users').doc(userId).collection('Portfolio');
      const portfolioSnapshot = await portfolioRef.get();
  
      if (portfolioSnapshot.empty) {
        return res.status(404).json({
          message: 'No portfolio found for this user.',
          portfolio: [],
        });
      }
  
      // Fetch portfolio data
      const portfolio = portfolioSnapshot.docs.map((doc) => {
        const stock = doc.data();
  
        return {
          id: doc.id, // Stock document ID
          name: stock.name || 'Unknown', // Stock name
          symbol: stock.symbol || 'N/A', // Stock symbol
          quantity: stock.quantity || 0, // Number of shares
          price: stock.averagePrice || 0, // Average purchase price
        };
      });
  
      // Return portfolio data
      res.status(200).json({ portfolio });
    } catch (error) {
      console.error('Error fetching portfolio:', error.message);
      res.status(500).json({ error: 'Internal server error.' });
    }
  });

  router.get('/clients/:userId', async (req, res) => {
    const { userId } = req.params;
    try {
      const clientsSnapshot = await db.collection('users').doc(userId).collection('Clients').get();
      const clients = clientsSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      res.status(200).json({ clients });
    } catch (error) {
      console.error('Error fetching clients:', error.message);
      res.status(500).json({ error: 'Failed to fetch clients.' });
    }
  });

  router.post('/clients/add/:userId', async (req, res) => {
    const { userId } = req.params; // Extract userId from the URL path
    const { clientName } = req.body; // Extract clientName from the request body
  
    if (!userId || !clientName) {
      return res.status(400).json({ error: 'Missing userId or clientName.' });
    }
  
    try {
      const clientRef = db.collection('users').doc(userId).collection('Clients').doc();
      const newClient = { name: clientName, balance: 1.99 /*signup bonus */};
  
      await clientRef.set(newClient);
  
      res.status(201).json({
        message: 'Client added successfully.',
        newClient: { id: clientRef.id, ...newClient },
      });
    } catch (error) {
      console.error('Error adding client:', error.message);
      res.status(500).json({ error: 'Failed to add client.' });
    }
  });

module.exports = router;