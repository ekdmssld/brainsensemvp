// controllers/authController.js - 인증 관련 로직
import User from '../models/User.js';
import jwt from 'jsonwebtoken';

// JWT 토큰 생성
const generateToken = (id) => {
    return jwt.sign({ id }, process.env.JWT_SECRET, {
        expiresIn: process.env.JWT_EXPIRE || '7d'
    });
};

// @desc    사용자 회원가입
// @route   POST /api/auth/register
// @access  Public
export const register = async (req, res) => {
    try {
        const { name, email, password, school, grade } = req.body;

        // 필수 필드 확인
        if (!name || !email || !password) {
            return res.status(400).json({
                success: false,
                message: '이름, 이메일, 비밀번호를 모두 입력해주세요'
            });
        }

        // 이메일 중복 확인
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            return res.status(400).json({
                success: false,
                message: '이미 사용 중인 이메일입니다'
            });
        }

        // 사용자 생성
        const user = await User.create({
            name,
            email,
            password,
            school,
            grade
        });

        // JWT 토큰 생성
        const token = generateToken(user._id);

        res.status(201).json({
            success: true,
            message: '회원가입이 완료되었습니다',
            token,
            user: {
                id: user._id,
                name: user.name,
                email: user.email,
                role: user.role,
                school: user.school,
                grade: user.grade
            }
        });
    } catch (error) {
        console.error('Register error:', error);
        res.status(500).json({
            success: false,
            message: '회원가입 중 오류가 발생했습니다',
            error: error.message
        });
    }
};

// @desc    사용자 로그인
// @route   POST /api/auth/login
// @access  Public
export const login = async (req, res) => {
    try {
        const { email, password } = req.body;

        // 필수 필드 확인
        if (!email || !password) {
            return res.status(400).json({
                success: false,
                message: '이메일과 비밀번호를 모두 입력해주세요'
            });
        }

        // 사용자 찾기 (비밀번호 포함)
        const user = await User.findOne({ email }).select('+password');
        if (!user) {
            return res.status(401).json({
                success: false,
                message: '이메일 또는 비밀번호가 올바르지 않습니다'
            });
        }

        // 비밀번호 확인
        const isPasswordMatch = await user.matchPassword(password);
        if (!isPasswordMatch) {
            return res.status(401).json({
                success: false,
                message: '이메일 또는 비밀번호가 올바르지 않습니다'
            });
        }

        // JWT 토큰 생성
        const token = generateToken(user._id);

        res.status(200).json({
            success: true,
            message: '로그인 성공',
            token,
            user: {
                id: user._id,
                name: user.name,
                email: user.email,
                role: user.role,
                school: user.school,
                grade: user.grade
            }
        });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({
            success: false,
            message: '로그인 중 오류가 발생했습니다',
            error: error.message
        });
    }
};

// @desc    현재 로그인한 사용자 정보 조회
// @route   GET /api/auth/me
// @access  Private
export const getMe = async (req, res) => {
    try {
        const user = await User.findById(req.user.id);

        res.status(200).json({
            success: true,
            user: {
                id: user._id,
                name: user.name,
                email: user.email,
                role: user.role,
                school: user.school,
                grade: user.grade,
                createdAt: user.createdAt
            }
        });
    } catch (error) {
        console.error('GetMe error:', error);
        res.status(500).json({
            success: false,
            message: '사용자 정보를 가져오는 중 오류가 발생했습니다',
            error: error.message
        });
    }
};

// @desc    로그아웃 (클라이언트에서 토큰 삭제)
// @route   POST /api/auth/logout
// @access  Private
export const logout = async (req, res) => {
    res.status(200).json({
        success: true,
        message: '로그아웃되었습니다'
    });
};