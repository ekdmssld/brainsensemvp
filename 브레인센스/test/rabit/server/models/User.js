// models/User.js - 사용자 모델
import mongoose from 'mongoose';
import bcrypt from 'bcryptjs';

const userSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, '이름을 입력해주세요'],
        trim: true
    },
    email: {
        type: String,
        required: [true, '이메일을 입력해주세요'],
        unique: true,
        lowercase: true,
        trim: true,
        match: [
            /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/,
            '유효한 이메일 주소를 입력해주세요'
        ]
    },
    password: {
        type: String,
        required: [true, '비밀번호를 입력해주세요'],
        minlength: [6, '비밀번호는 최소 6자 이상이어야 합니다'],
        select: false // 기본 쿼리에서 제외
    },
    role: {
        type: String,
        enum: ['student', 'teacher', 'admin'],
        default: 'student'
    },
    school: {
        type: String,
        trim: true
    },
    grade: {
        type: String,
        trim: true
    },
    createdAt: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

// 비밀번호 해싱 (저장 전)
userSchema.pre('save', async function(next) {
    // 비밀번호가 수정되지 않았으면 스킵
    if (!this.isModified('password')) {
        next();
    }

    // 비밀번호 해싱
    const salt = await bcrypt.genSalt(10);
    this.password = await bcrypt.hash(this.password, salt);
});

// 비밀번호 확인 메서드
userSchema.methods.matchPassword = async function(enteredPassword) {
    return await bcrypt.compare(enteredPassword, this.password);
};

const User = mongoose.model('User', userSchema);

export default User;