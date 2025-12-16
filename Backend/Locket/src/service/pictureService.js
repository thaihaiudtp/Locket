const User = require('../model/User');
const Picture = require('../model/Picture');

class PictureService {
    uploadPicture = async (file, userDecoded) => {
        if (!file) {
            throw new Error('No image file provided');
        }
        const user = await User.findOne({ email: userDecoded.email });
        if (!user) {
            throw new Error('User not found');
        }
        const newPicture = new Picture({
            url: file.path, 
            uploader: user._id
        });
        await newPicture.save();
        user.pictures.push(newPicture._id);
        await user.save();
        return {
            success: true,
            message: 'Upload successfully',
            data: newPicture
        };
    }
}
module.exports = new PictureService();