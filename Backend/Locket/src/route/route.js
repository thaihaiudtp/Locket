
const express = require('express');
const authRoutes = require('./auth');
const pictureRoute = require('./picture');
const router = express.Router();
const base = process.env.BASE_URL || '/api/v1';
router.use(`${base}/auth`, authRoutes);
router.use(`${base}/picture`, pictureRoute);
module.exports = router;