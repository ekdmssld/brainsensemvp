// models/Kit.js - 키트 모델
import mongoose from 'mongoose';

const kitSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, '키트 이름을 입력해주세요'],
        trim: true
    },
    category: {
        type: String,
        required: [true, '카테고리를 입력해주세요'],
        enum: ['스마트 인솔', '스마트 피아노', '압력 쿠션', '기타'],
        default: '기타'
    },
    description: {
        type: String,
        required: [true, '설명을 입력해주세요']
    },
    image: {
        type: String,
        default: '../clients/assets/images/kits/kit_1.jpeg'
    },
    difficulty: {
        type: Number,
        min: 1,
        max: 3,
        default: 1
    },
    duration: {
        type: Number, // 분 단위
        default: 60
    },
    components: [{
        type: String // 구성품 목록
    }],
    learningObjectives: [{
        type: String // 학습 목표
    }],
    sensors: [{
        name: { type: String },
        type: { type: String },
        pin: { type: String },
        description: { type: String }
    }],
    isActive: {
        type: Boolean,
        default: true
    },
    order: {
        type: Number,
        default: 0
    }
}, {
    timestamps: true
});

const Kit = mongoose.model('Kit', kitSchema);

export default Kit;