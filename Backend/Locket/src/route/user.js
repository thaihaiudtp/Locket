const express = require('express');
const userController = require('../controller/userController');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.get('/search', authMiddleware, userController.searchUser);
router.post('/friend-request', authMiddleware, userController.sendFriendRequest);
router.get('/friends', authMiddleware, userController.listMyFriends);
router.get('/friend-requests-recived', authMiddleware, userController.listMyFriendsRequests);
router.get('/friend-requests-sent', authMiddleware, userController.listMyFriendRequestsSent);
router.post('/friend-request/accept', authMiddleware, userController.acceptFriendRequest);
router.post('/friend-request/reject', authMiddleware, userController.rejectFriendRequest);
module.exports = router;