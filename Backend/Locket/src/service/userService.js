const User = require('../model/User');
const FriendRequest = require('../model/friend');
class UserService {
    searchUserByUsername = async (username, userId) => {
        const filter = {
            _id: { $ne: userId }
        };
        if (username && username.trim() !== '') {
            filter.username = { $regex: username, $options: 'i' };
        }
        const users = await User.find(filter)
            .select('username email friends');

        const userIds = users.map(u => u._id);
        const friendRequests = await FriendRequest.find({
            $or: [
                { senderId: userId, receiverId: { $in: userIds } },
                { receiverId: userId, senderId: { $in: userIds } }
            ]
        });
        const requestMap = new Map();
        friendRequests.forEach(fr => {
            const otherUserId =
                fr.senderId.toString() === userId.toString()
                    ? fr.receiverId.toString()
                    : fr.senderId.toString();

            requestMap.set(otherUserId, fr);
        });
        const result = users.map(user => {
            let relationshipStatus = 'none';
            if (user.friends.some(id => id.equals(userId))) {
                relationshipStatus = 'friend';
            }
            const fr = requestMap.get(user._id.toString());
            if (fr) {
                if (fr.senderId.equals(userId)) {
                    relationshipStatus = 'sent';
                } else {
                    relationshipStatus = 'received';
                }
            }

            return {
                _id: user._id,
                username: user.username,
                email: user.email,
                relationshipStatus
            };
        });
        return {
            success: true,
            message: 'Users found',
            data: result
        };
    };
    sendFriendRequest = async (senderId, receiveId) => {
        await FriendRequest.create({
            senderId,
            receiverId: receiveId
        });
        return ({
            success: true,
            message: 'Friend request sent',
        })
    }
    listMyFriendRequests = async (userId) => {
        const requests = await FriendRequest.find({
            receiverId: userId,
            status: 'pending'
        }).populate('senderId', 'username email');
        return {
            success: true,
            message: 'Friend requests retrieved successfully',
            data: requests
        };
    }
    listMyFriendRequestSent = async (userId) => {
        const requests = await FriendRequest.find({
            senderId: userId,
            status: 'pending'   
        }).populate('receiverId', 'username email');
        return {
            success: true,
            message: 'Sent friend requests retrieved successfully',
            data: requests
        };
    }
    listMyFriends = async (userId) => {
        const user = await User.findById(userId).populate('friends', 'username email');
        if (!user) {
            throw new Error('User not found');
        }
        return {
            success: true,
            message: 'Friends retrieved successfully',
            data: user.friends
        };
    }
    acceptFriendRequest = async (requestId, userId) => {
        const friendRequest = await FriendRequest.findById(requestId);
        if (!friendRequest) {
            throw new Error('Friend request not found');
        }
        if (!friendRequest.receiverId.equals(userId)) {
            throw new Error('Not authorized to accept this friend request');
        }
        friendRequest.status = 'accepted';
        await friendRequest.save();
        await User.findByIdAndUpdate(friendRequest.senderId, {  
            $push: { friends: friendRequest.receiverId }
        });
        await User.findByIdAndUpdate(friendRequest.receiverId, {    
            $push: { friends: friendRequest.senderId }
        });
        return {
            success: true,
            message: 'Friend request accepted'
        };
    }
    rejectFriendRequest = async (requestId, userId) => {
        const friendRequest = await FriendRequest.findById(requestId);
        if (!friendRequest) {
            throw new Error('Friend request not found');
        }
        if (!friendRequest.receiverId.equals(userId)) {
            throw new Error('Not authorized to reject this friend request');
        }
        await friendRequest.deleteOne();
        return {
            success: true,
            message: 'Friend request rejected'
        };
    }
    getUserById = async (userId) => {
        if (!userId) {
            throw new Error('User ID is required');
        }
        const user = await User.findById(userId).select('username email friends');
        if (!user) {
            throw new Error('User not found');
        }
        return {
            success: true,
            message: 'User retrieved successfully',
            data: user
        };
    }
}
module.exports = new UserService();