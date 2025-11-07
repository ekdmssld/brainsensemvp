// routes/projects.js - 프로젝트 관련 라우트
import express from 'express';
import {
    createProject,
    updateProject,
    getMyProjects,
    getProjectById
} from '../controllers/projectController.js';
import { protect } from '../middleware/auth.js';

const router = express.Router();

// 모든 라우트에 인증 적용
router.use(protect);

router.route('/')
    .get(getMyProjects)
    .post(createProject);

router.route('/:id')
    .get(getProjectById)
    .put(updateProject);

export default router;