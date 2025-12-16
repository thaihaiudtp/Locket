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
}

module.exports = new PictureController();