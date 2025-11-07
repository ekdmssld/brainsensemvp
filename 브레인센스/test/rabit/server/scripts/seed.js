// scripts/seed.js - 테스트 데이터 생성
import mongoose from 'mongoose';
import dotenv from 'dotenv';
import User from '../models/User.js';

dotenv.config();

const seedUsers = async () => {
    try {
        await mongoose.connect(process.env.MONGODB_URI);
        console.log('MongoDB Connected');

        // 기존 데이터 삭제
        await User.deleteMany();
        console.log('Existing users deleted');

        // 테스트 사용자 생성
        const users = await User.create([
            {
                name: '테스트 학생',
                email: 'test@labit.com',
                password: 'test1234',
                role: 'student',
                school: '부산일과학고등학교',
                grade: '2학년'
            },
            {
                name: '김선생',
                email: 'teacher@labit.com',
                password: 'teacher1234',
                role: 'teacher',
                school: '부산일과학고등학교'
            },
            {
                name: '관리자',
                email: 'admin@labit.com',
                password: 'admin1234',
                role: 'admin'
            }
        ]);

        console.log('T est users created:');
        users.forEach(user => {
            console.log(`   - ${user.email} (${user.role})`);
        });

        process.exit(0);
    } catch (error) {
        console.error('Seed error:', error);
        process.exit(1);
    }
};

seedUsers();