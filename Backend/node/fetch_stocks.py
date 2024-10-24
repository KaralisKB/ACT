import yfinance as yf
import firebase_admin
from firebase_admin import credentials, firestore
import sys

cred = credentials.Certificate()