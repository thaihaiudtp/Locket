const User = require('../model/User');
const Picture = require('../model/Picture');
require("../model/Icon")
class PictureService {
    uploadPicture = async (file, userDecoded, message = '', time = '', location = '') => {
        if (!file) {
            throw new Error('No image file provided');
        }
        const user = await User.findOne({ email: userDecoded.email });
        if (!user) {
            throw new Error('User not found');
        }
        const newPicture = new Picture({
            url: file.path,
            uploader: user._id,
            ...(message && { message }),
            ...(time && { time }),
            ...(location && { location })
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
    listPictures = async (request, page, limit) => {
        const email = request.email;
        const skip = (page - 1) * limit;

        const user = await User.findOne({ email: email }).select('friends');
        if (!user) {
            return {
                success: false,
                message: 'User not found'
            };
        }
        const friendIds = user.friends;
        const uploaderIds = [user._id, ...friendIds];
        const [pictures, total] = await Promise.all([
            Picture.find({ uploader: { $in: uploaderIds } })
                .select('url uploader message time location createdAt') // chỉ lấy field cần
                .populate('uploader', 'username') 
                .sort({ createdAt: -1 })
                .skip(skip)
                .limit(limit)
                .lean(),

            Picture.countDocuments({ uploader: { $in: uploaderIds } })
        ]);

        return {
            success: true,
            message: 'Pictures retrieved successfully',
            meta: {
                total,
                page,
                limit,
                totalPages: Math.ceil(total / limit)
            },
            data: pictures,
        };
    }
    detailPicture = async (pictureId) => {
        const picture = await Picture.findById(pictureId)
            .populate('uploader', 'username email')
            .populate('reactions.icon')
            .populate('reactions.user', 'username email');
        if (!picture) {
            throw new Error('Picture not found');
        }
        return {
            success: true,
            message: 'Picture retrieved successfully',
            data: picture
        }
    }
}
module.exports = new PictureService();