// models/Project.js - 프로젝트 모델
import mongoose from 'mongoose';

const projectSchema = new mongoose.Schema({
    user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    kitId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Kit',
        required: true
    },
    kitName: {
        type: String,
        required: true
    },
    // 브레인스토밍 데이터
    brainstorming: {
        A: [{
            id: String,
            content: String,
            x: Number,
            y: Number,
            color: String
        }],
        B: [{
            id: String,
            content: String,
            x: Number,
            y: Number,
            color: String
        }],
        C: [{
            id: String,
            content: String,
            x: Number,
            y: Number,
            color: String
        }]
    },
    // 회로 설계 데이터 (나중에 추가)
    hardwareDesign: {
        type: Object,
        default: {}
    },
    // 코딩 데이터 (나중에 추가)
    code: {
        type: String
    },
    // AI 생성 코드
    generatedCode: {
        type: String
    },
    // 프로젝트 상태
    status: {
        type: String,
        enum: ['brainstorming', 'hardware', 'coding', 'testing', 'completed'],
        default: 'brainstorming'
    }
}, {
    timestamps: true
});

const Project = mongoose.model('Project', projectSchema);

export default Project;