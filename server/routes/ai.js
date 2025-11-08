// routes/ai.js - AI 관련 라우트
import express from 'express';
import {
    generateCodeLine,
    finalizeCode,
    explainCode,
    checkAIHealth,
    trainRules,
    getModel,
    predictValue
} from '../controllers/aiController.js';
import { protect } from '../middleware/auth.js';

const router = express.Router();

router.use(protect);

router.get('/health', checkAIHealth);
router.post('/generate-code-line', generateCodeLine);
router.post('/finalize-code', finalizeCode);
router.post('/explain-code', explainCode);
router.post('/train-rules', trainRules);
router.get('/models/:id', getModel);
router.post('/models/:id/predict', predictValue);

export default router;