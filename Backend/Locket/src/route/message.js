const express = require('express');
const messageController = require('../controller/messageController');
const authMiddleware = require('../middleware/auth');

const router = express.Router();
router.get('/get-conversations', authMiddleware, messageController.getConversations);
router.post('/send-message', authMiddleware, messageController.sendMessage);
router.get('/conversation/:id/', authMiddleware, messageController.getMessagesInConversation);
module.exports = router;