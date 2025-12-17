const UserService = require('../service/userService');

class UserController {
    searchUser = async (req, res) => {
        try {
            const query = req.query.search; 
            const userEmail = req.user.email;   

            if (!query) {
                return res.status(400).json({
                    success: false,
                    message: 'Search keyword is required'
                });
            }

            const response = await UserService.searchUserByUsername(query, userEmail);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            });
        }
    };

}

module.exports = new UserController();