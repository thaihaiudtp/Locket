const mongoose = require('mongoose');
const { Schema } = mongoose;

const iconSchema = new Schema(
  {
    name: {
      type: String,
      required: true,
      unique: true
    },

    url: {
      type: String 
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.model('Icon', iconSchema);
