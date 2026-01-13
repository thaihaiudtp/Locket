const messageService = require('../service/messageService');
class MessageController {
    getConversations = async (req, res) => {
        try {
            const senderId = req.user.id;
            const page = parseInt(req.query.page) || 1;
            const limit = parseInt(req.query.limit) || 20;
            const response = await messageService.getConversations(senderId, page, limit);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            })
        }
    }
    sendMessage = async (req, res) => {
        try {
            const senderId = req.user.id;
            const { receiverId, content, attachedPictureId } = req.body;
            const response = await messageService.sendMessage(senderId, receiverId, content, attachedPictureId);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            })
        }
    }
    getMessagesInConversation = async (req, res) => {
        try {
            const conversationId = req.params.id;
            const page = parseInt(req.query.page) || 1;
            const limit = parseInt(req.query.limit) || 20;  
            const response = await messageService.getMessagesInConversation(conversationId, page, limit);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({
                success: false,
                message: error.message
            })
        }
    }
}
module.exports = new MessageController();
