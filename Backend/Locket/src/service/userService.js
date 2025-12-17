const User = require('../model/User');
class UserService {
    searchUserByUsername = async (username, currentUserId) => {
        const users = await User.find({
            _id: { $ne: currentUserId }, 
            username: { $regex: username, $options: 'i' }
        })
        .select('username email'); // không trả password

        return {
            success: true,
            message: 'Users found',
            data: users
        };
    };

}
module.exports = new UserService();