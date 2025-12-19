const UserService = require('../service/userService');
class UserController {
    searchUser = async (req, res) => {
        try {
            const query = req.query.search || ""; 
            const userId = req.user.id;   
            const response = await UserService.searchUserByUsername(query, userId);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            });
        }
    };
    sendFriendRequest = async (req, res) => {
        try {
            const senderId = req.user.id;
            const { receiverId } = req.body;
            const response = await UserService.sendFriendRequest(senderId, receiverId);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            });
        }
    }
    listMyFriends = async (req, res) => {
        try {
            const userId = req.user.id;
            const response = await UserService.listMyFriends(userId);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            })
        }
    }
    listMyFriendsRequests = async (req, res) => {
        try {
            const userId = req.user.id;
            const response = await UserService.listMyFriendRequests(userId);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            })
        }
    }
    listMyFriendRequestsSent = async (req, res) => {
        try {
            const userId = req.user.id;
            const response = await UserService.listMyFriendRequestSent(userId);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            })
        }
    }
    acceptFriendRequest = async (req, res) => {
        try {
            const userId = req.user.id;
            const { requestId } = req.body;
            const response = await UserService.acceptFriendRequest(requestId, userId);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            })
        }
    }
    rejectFriendRequest = async (req, res) => {
        try {
            const userId = req.user.id;
            const { requestId } = req.body;
            const response = await UserService.rejectFriendRequest(requestId, userId);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            })
        }   
    }
}

module.exports = new UserController();