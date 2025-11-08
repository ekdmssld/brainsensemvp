// middleware/auth.js - JWT 토큰 검증 미들웨어
import jwt from 'jsonwebtoken';
import User from '../models/User.js';

export const protect = async (req, res, next) => {
    let token;

    // Authorization 헤더에서 토큰 추출
    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
        token = req.headers.authorization.split(' ')[1];
    }

    // 토큰이 없으면 에러
    if (!token) {
        return res.status(401).json({
            success: false,
            message: '인증 토큰이 없습니다. 로그인해주세요.'
        });
    }

    try {
        // 토큰 검증
        const decoded = jwt.verify(token, process.env.JWT_SECRET);

        // 사용자 정보 조회
        req.user = await User.findById(decoded.id).select('-password');

        if (!req.user) {
            return res.status(401).json({
                success: false,
                message: '사용자를 찾을 수 없습니다'
            });
        }

        next();
    } catch (error) {
        console.error('Auth middleware error:', error);

        if (error.name === 'TokenExpiredError') {
            return res.status(401).json({
                success: false,
                message: '토큰이 만료되었습니다. 다시 로그인해주세요.'
            });
        }

        return res.status(401).json({
            success: false,
            message: '유효하지 않은 토큰입니다'
        });
    }
};

// 역할 기반 접근 제어
export const authorize = (...roles) => {
    return (req, res, next) => {
        if (!roles.includes(req.user.role)) {
            return res.status(403).json({
                success: false,
                message: `${req.user.role} 권한으로는 이 작업을 수행할 수 없습니다`
            });
        }
        next();
    };
};