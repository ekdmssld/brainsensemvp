// server.js - 메인 서버 엔트리포인트
import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import connectDB from './config/db.js';
import authRoutes from './routes/auth.js';
import { errorHandler } from './middleware/errorHandler.js';
import kitRoutes from './routes/kits.js';
import projectRoutes from './routes/projects.js';
import aiRoutes from './routes/ai.js';

// 환경 변수 로드
dotenv.config();

// Express 앱 생성
const app = express();

// MongoDB 연결
connectDB();

// 미들웨어
app.use(cors({
    origin: process.env.CLIENT_URL || 'http://localhost:8080',
    credentials: true
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 라우트
app.use('/api/auth', authRoutes);
app.use('/api/kits', kitRoutes);
app.use('/api/projects', projectRoutes);
app.use('/api/ai', aiRoutes);

// 헬스체크 엔드포인트
app.get('/api/health', (req, res) => {
    res.json({
        status: 'OK',
        message: 'LABIT Server is running',
        timestamp: new Date().toISOString()
    });
});

// 에러 핸들러 (마지막에 위치)
app.use(errorHandler);

// 서버 시작
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
    console.log(`Environment: ${process.env.NODE_ENV}`);
});