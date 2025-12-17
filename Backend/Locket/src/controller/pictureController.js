const PictureService = require('../service/pictureService');
class PictureController {
    uploadPicture = async(req, res) => {
        const file = req.file;
        const userDecoded = req.user;
        try {
            const response = await PictureService.uploadPicture(file, userDecoded);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({success: false, message: error.message});
        }
    }
    listPictures = async(req, res) => {
        const userDecoded = req.user;
        const page = req.query.page;
        const limit = req.query.limit;
        try {
            const response = await PictureService.listPictures(userDecoded, page, limit);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({success: false, message: error.message});
        }
    }
    detailPicture = async(req, res) => {
        const pictureId = req.params.id;
        try {
            const response = await PictureService.detailPicture(pictureId);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({success: false, message: error.message});
        }
    }
}

module.exports = new PictureController();