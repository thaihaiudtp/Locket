const express = require('express');
const userController = require('../controller/userController');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.get('/search', authMiddleware, userController.searchUser);
module.exports = router;