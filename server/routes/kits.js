// server/routes/kits.js
import express from 'express';
import {
    getKits,
    getKitsByCategory,
    getKitById,
    createKit,
    getKitComponents,
    getComponentDetail,
} from '../controllers/kitController.js';
// import { protect } from '../middleware/auth.js';

const router = express.Router();

// router.use(protect);

// 목록/카테고리
router.get('/', getKits);
router.get('/category/:category', getKitsByCategory);

// (Step3) 컴포넌트 목록/상세
router.get('/:kitId/components', getKitComponents);
router.get('/:kitId/components/:componentId', getComponentDetail);

// 단건 상세
router.get('/:id', getKitById);

// 생성(관리자)
router.post('/', createKit);

export default router;