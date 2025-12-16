const AuthService = require('../service/authService');
class AuthController {
    register = async(req, res) => {
        const request = req.body;
        try {
            const response = await AuthService.register(request);
            res.status(201).json(response);
        } catch (error) {
            res.status(400).json({success: false, message: error.message});
        }
    } 
    login = async(req, res) => {
        const request = req.body;   
        try {
            const response = await AuthService.login(request);
            res.status(200).json(response);
        } catch (error) {
            res.status(400).json({success: false, message: error.message});
        }
    }
}

module.exports = new AuthController();