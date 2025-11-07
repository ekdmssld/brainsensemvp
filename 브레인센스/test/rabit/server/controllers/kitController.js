// controllers/kitController.js - 키트 관련 로직
import Kit from '../models/Kit.js';

// @desc    모든 키트 조회
// @route   GET /api/kits
// @access  Private
export const getKits = async (req, res) => {
    try {
        const kits = await Kit.find({ isActive: true }).sort({ order: 1, createdAt: -1 });

        res.status(200).json({
            success: true,
            count: kits.length,
            kits
        });
    } catch (error) {
        console.error('Get kits error:', error);
        res.status(500).json({
            success: false,
            message: '키트 목록을 불러오는데 실패했습니다',
            error: error.message
        });
    }
};

// @desc    키트 상세 조회
// @route   GET /api/kits/:id
// @access  Private
export const getKitById = async (req, res) => {
    try {
        const kit = await Kit.findById(req.params.id);

        if (!kit) {
            return res.status(404).json({
                success: false,
                message: '키트를 찾을 수 없습니다'
            });
        }

        res.status(200).json({
            success: true,
            kit
        });
    } catch (error) {
        console.error('Get kit error:', error);
        res.status(500).json({
            success: false,
            message: '키트 정보를 불러오는데 실패했습니다',
            error: error.message
        });
    }
};

// @desc    카테고리별 키트 조회
// @route   GET /api/kits/category/:category
// @access  Private
export const getKitsByCategory = async (req, res) => {
    try {
        const kits = await Kit.find({
            category: req.params.category,
            isActive: true
        }).sort({ order: 1 });

        res.status(200).json({
            success: true,
            count: kits.length,
            kits
        });
    } catch (error) {
        console.error('Get kits by category error:', error);
        res.status(500).json({
            success: false,
            message: '키트 목록을 불러오는데 실패했습니다',
            error: error.message
        });
    }
};

// @desc    키트 생성 (관리자)
// @route   POST /api/kits
// @access  Private/Admin
export const createKit = async (req, res) => {
    try {
        const kit = await Kit.create(req.body);

        res.status(201).json({
            success: true,
            message: '키트가 생성되었습니다',
            kit
        });
    } catch (error) {
        console.error('Create kit error:', error);
        res.status(500).json({
            success: false,
            message: '키트 생성에 실패했습니다',
            error: error.message
        });
    }
};