const express = require('express');
const pictureController = require('../controller/pictureController');
const authMiddleware = require('../middleware/auth');
const upload = require('../config/uploadFile');
const router = express.Router();

router.post('/upload', authMiddleware, upload.single('file'), pictureController.uploadPicture);
router.get('/detail/:id', authMiddleware, pictureController.detailPicture);
router.get('/list', authMiddleware, pictureController.listPictures);
module.exports = router;