const mongoose = require('mongoose');
const { Schema } = mongoose;

const messageSchema = new Schema(
  {
    participants: {
      type: [{ type: Schema.Types.ObjectId, ref: 'User', required: true }],
      validate: v => Array.isArray(v) && v.length === 2
    },
    pairKey: { type: String, required: true, unique: true },
    messages: [
      {
        sender: { type: Schema.Types.ObjectId, ref: 'User', required: true },
        content: { type: String, default: '' },
        attachedPicture: { type: Schema.Types.ObjectId, ref: 'Picture' },
        readBy: [{ type: Schema.Types.ObjectId, ref: 'User' }],
        createdAt: { type: Date, default: Date.now }
      }
    ],
    lastMessageAt: { type: Date, default: Date.now }
  },
  { timestamps: true }
);

messageSchema.pre('validate', function () {
  if (this.participants?.length === 2) {
    const sorted = this.participants.map(id => id.toString()).sort();
    this.pairKey = `${sorted[0]}:${sorted[1]}`;
  }
});

messageSchema.pre('save', function () {
  if (this.messages?.length) {
    const last = this.messages[this.messages.length - 1];
    this.lastMessageAt = last.createdAt || Date.now();
  }
});

// index hỗ trợ truy vấn
messageSchema.index({ pairKey: 1 }, { unique: true });
messageSchema.index({ lastMessageAt: -1 });

module.exports = mongoose.model('Message', messageSchema);