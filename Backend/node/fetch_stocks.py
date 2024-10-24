import yfinance as yf
import firebase_admin
from firebase_admin import credentials, firestore
import sys
import os
import json 

firebase_credentials_json = os.getenv('FIREBASE_CREDENTIALS')
firebase_credentials = json.loads(firebase_credentials_json)

cred = credentials.Certificate(firebase_credentials)
firebase_admin.initialize_app(cred)
db = firestore.client()

def update_stock(symbols):
    tickers = yf.Tickers(" ".join(symbols))

    for symbol in symbols:
        stock = tickers.tickers[symbol]
        try:
            price = stock.fast_info["last_price"]

            doc_ref = db.collection('stocks').document(symbol)
            doc_ref.set({
                'symbol': symbol,
                'price': price,
                'lastUpdated': firestore.SERVER_TIMESTAMP
            })
            print(f"Updated {symbol} price: {price}")
        except Exception as e:
            print(f"Error updating {symbol}: {e}")

if __name__ == "__main__":
    