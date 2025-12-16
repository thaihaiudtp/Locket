const User = require('../model/User');
const genToken = require('../utils/genToken');

class AuthService {
    register = async (request) => {
        const {username, email, password} = request;
        if (!username || !email || !password) {
            throw new Error('All fields are required');
        }
        const existingUser = await User.findOne({$or: [{email}, {username}]});
        if (existingUser) {
            throw new Error('Username or email already in use');
        }
        const newUser = new User({username, email, password});
        await newUser.save();
        return {
            success: true,
            message: 'User registered successfully',
        }
    }
    login = async (request) => {
        const {email, password} = request;
        if (!email || !password) {
            throw new Error('Email and password are required');
        }
        const user = await User.findOne({email});
        if (!user) {
            throw new Error('Invalid email or password');
        }
        const isMatch = await user.comparePassword(password);
        if (!isMatch) {
            throw new Error('Invalid email or password');
        }
        const token = genToken(user);
        return {
            success: true,
            message: 'Login successful',
            token
        };
    }
}

module.exports = new AuthService();