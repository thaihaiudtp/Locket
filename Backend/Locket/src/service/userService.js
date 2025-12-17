const User = require('../model/User');
class UserService {
    searchUserByUsername = async (username, userId) => {
        const users = await User.find({
            _id: { $ne: userId }, 
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