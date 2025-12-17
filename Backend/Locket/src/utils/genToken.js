const jwt = require('jsonwebtoken');
const dotenv = require('dotenv');
dotenv.config();
const genToken = (user) => {
    return jwt.sign(
        {
            id: user._id,
            email: user.email,
            username: user.username
        },
            process.env.JWT_SECRET,
        {
            expiresIn: '7d' 
        }
    );
};

module.exports = genToken;
