## Stocks Portfolio Manager React by *LuluLogics & Team*

## Overview:
- Stocks Portfolio management web application which enables the user to view stock listings, IPO listings, add stocks to watchlist, buy/sell stocks, display the portfolio, stock transaction history and showcase the stock news.

- Server Deployed on Heroku and Client deployed on Netlify


## Functionality:
* Landing Page: 
Landing Page will show the features that we offer. 

* Register Page:
Register Page will have a form to be filled by user to Create a new account.

* Login:
Login Page for the existing users to login.

* Home Page:
Home page will have stock listings with real time stock value fetched using finnhub.io API

* Watchlist: 
Contains user added stocks with real time values such as stock price, volume, day high, day low, etc.

* Buy/Sell Stocks:
User can buy/sell stocks. 

* Portfolio: 
Showcases user with current investment, quantity and profit/loss for each stock holdings and total profit/loss.

* Stock Information:
Contains realtime stock chart and technical analysis of that stock. 

## Folder Structure
 	# Backend Directory
    .
    ├── app.js                  #  all modules imported in this file and mongoDB connection          
    ├── server.js               #  Start point of the code
    ├── api
         ├──controllers         # controllers for each task and are called by routes
         ├──models              # contains the schema of all collections in MongoDB
         ├──routes              # contains the routes according to URL and request methods
         └──services            # contains the business logic of all the operations

    # Frontend Directory

    frontend
    ├── src
    |   |── Cards               # react components
    |   |── Charts              
    |   |── components          
    |   |── contexts  
    |   |── global              # contains react components on each page
    |   |── hooks               # hooks used for login and trade transaction
    |   └── scenes 
    |         |── dashboard     # contains react components of pages
    |         |── login          
    |         └── register
    └── app.js                  # contains the logic to fetch all items and call all the react components.
