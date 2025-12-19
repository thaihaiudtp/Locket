const mongoose = require('mongoose');
const { Schema } = mongoose;

const friendRequestSchema = new Schema(
{
    senderId: {
        type: Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    receiverId: {
        type: Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    status: {
        type: String,
        enum: ['pending', 'accepted', 'rejected'],
        default: 'pending'
    }
}, {
    timestamps: true
}
);
friendRequestSchema.index(
  { senderId: 1, receiverId: 1 },
  { unique: true }
);
friendRequestSchema.pre('save', function () {
  if (this.senderId.equals(this.receiverId)) {
    throw new Error('Cannot send friend request to yourself');
  }
});


module.exports = mongoose.model('FriendRequest', friendRequestSchema);
