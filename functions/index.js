/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotificationOnNewMessage = functions.firestore
    .document('chats/{chatId}/messages/{messageId}')
    .onCreate((snap, context) => {
        const message = snap.data();

        const payload = {
            notification: {
                title: `New message from ${message.senderName}`,
                body: message.text,
            },
            token: message.receiverToken, // Token korisnika koji prima poruku
        };

        return admin.messaging().send(payload)
            .then(response => {
                console.log('Successfully sent message:', response);
            })
            .catch(error => {
                console.log('Error sending message:', error);
            });
    });

