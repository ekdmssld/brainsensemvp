// routes/kits.js - 키트 관련 라우트
import express from 'express';
import {
    getKits,
    getKitById,
    getKitsByCategory,
    createKit
} from '../controllers/kitController.js';
import { protect, authorize } from '../middleware/auth.js';

const router = express.Router();

// 모든 라우트에 인증 적용
router.use(protect);

router.get('/', getKits);
router.get('/:id', getKitById);
router.get('/category/:category', getKitsByCategory);

// 관리자만 접근 가능
router.post('/', authorize('admin'), createKit);

export default router;