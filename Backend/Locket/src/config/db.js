const mongoose = require('mongoose');

async function connectDB() {
    await mongoose.connect(process.env.MONGODB_URL)
        .then(() => {
            console.log('Connected to MongoDB');
        })
        .catch((err) => {
            console.error('Error connecting to MongoDB:', err);
            process.exit(1);
        });
}
module.exports = connectDB;