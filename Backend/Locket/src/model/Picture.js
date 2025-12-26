const mongoose = require('mongoose');
const {Schema} = mongoose;

const pictureSchema = new Schema({
    url: {type: String, required: true},
    uploadAt: {type: Date, default: Date.now},
    uploader: {
        type: Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    reactions: [
        {
            icon: {
                type: Schema.Types.ObjectId,
                ref: 'Icon',
                required: true
            },
            user: {
                type: Schema.Types.ObjectId,
                ref: 'User',
                required: true
            },
            createdAt: {
                type: Date,
                default: Date.now
            }
        }
    ],
    message: {type: String, default: ''},
    time: {type: String, default: ''},
    location: {type: String, default: ''},
}, {
    timestamps: true
});

module.exports = mongoose.model('Picture', pictureSchema);