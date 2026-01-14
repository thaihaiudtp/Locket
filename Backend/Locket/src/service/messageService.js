const User = require('../model/User');
const Message = require('../model/Message');
const Picture = require('../model/Picture');
class MessageService {
    getConversations = async (senderId, page = 1, limit = 20) => {
        const skip = (page - 1) * limit;
        const messages = await Message.find({
            participants: senderId
        })
        .sort({ lastMessageAt: -1 })
        .skip(skip)
        .limit(limit)
        .select('_id participants pairKey lastMessageAt')
        .populate('participants', 'username')
        .lean();
        return {
            success: true,
            message: 'Conversations retrieved successfully',
            data: messages
        }
    }
    sendMessage = async (senderId, receiverId, content = '', attachedPictureId = null) => {
        const participants = [senderId, receiverId];
        const sorted = participants.map(id => id.toString()).sort();
        const pairKey = `${sorted[0]}:${sorted[1]}`;
        let messageThread = await Message.findOne({ pairKey });
        if (!messageThread) {
            messageThread = new Message({
                participants,
                pairKey,
                messages: []
            });
        }
        const newMessage = {
            sender: senderId,
            content,
            attachedPicture: attachedPictureId
        };
        messageThread.messages.push(newMessage);    
        await messageThread.save();
        return {
            success: true,
            message: 'Message sent successfully',
        }
    }
    getMessagesInConversation = async (conversationId, page = 1, limit = 20) => {
        const skip = (page - 1) * limit;
        const messageThread = await Message.findById(conversationId)
            .populate('messages.attachedPicture', 'url');
        if (!messageThread) {
            throw new Error('Conversation not found');
        }

        const totalMessages = messageThread.messages.length;
        const messages = messageThread.messages
            .map(m => (m.toObject ? m.toObject() : m))
            .sort((a, b) => b.createdAt - a.createdAt)
            .slice(skip, skip + limit)
            .map(m => ({
                ...m,
                attachmentUrl: m.attachedPicture?.url || null,
            }));

        return {
            success: true,
            message: 'Messages retrieved successfully',
            meta: {
                total: totalMessages,
                page,
                limit
            },
            data: {
                messages
            }
        };
    }
}

module.exports = new MessageService();