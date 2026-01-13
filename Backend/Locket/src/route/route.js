
const express = require('express');
const authRoutes = require('./auth');
const pictureRoute = require('./picture');
const user = require('./user');
const message = require('./message');
const router = express.Router();
const base = process.env.BASE_URL || '/api/v1';
router.use(`${base}/auth`, authRoutes);
router.use(`${base}/picture`, pictureRoute);
router.use(`${base}/user`, user);
router.use(`${base}/message`, message);
module.exports = router;