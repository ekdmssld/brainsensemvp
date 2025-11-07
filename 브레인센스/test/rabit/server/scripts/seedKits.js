// scripts/seedKits.js - 키트 테스트 데이터 생성
import mongoose from 'mongoose';
import dotenv from 'dotenv';
import Kit from '../models/Kit.js';

dotenv.config();

const seedKits = async () => {
    try {
        await mongoose.connect(process.env.MONGODB_URI);
        console.log('✅ MongoDB Connected');

        // 기존 키트 데이터 삭제
        await Kit.deleteMany();
        console.log('🗑️  Existing kits deleted');

        // 테스트 키트 생성
        const kits = await Kit.create([
            {
                name: '스마트 인솔 - 보행 분석',
                category: '스마트 인솔',
                description: '압력 센서를 활용하여 보행 패턴을 분석하고 AI로 걸음걸이를 평가하는 스마트 인솔 프로젝트',
                image:'../assets/images/kits/kit_1.jpeg',
                difficulty: 2,
                duration: 90,
                components: [
                    '아두이노 Nano',
                    '압력 센서 (FSR) x4',
                    '블루투스 모듈 (HC-06)',
                    '배터리 홀더',
                    '점퍼 와이어',
                    '인솔 베이스'
                ],
                learningObjectives: [
                    '압력 센서의 원리와 활용 방법 이해',
                    '센서 데이터 수집 및 처리 과정 학습',
                    'AI를 활용한 보행 패턴 분석 체험',
                    '블루투스 무선 통신 구현'
                ],
                sensors: [
                    { name: '앞쪽 압력센서', type: 'FSR402', pin: 'A0', description: '앞발 압력 측정' },
                    { name: '뒤쪽 압력센서', type: 'FSR402', pin: 'A1', description: '뒷발 압력 측정' }
                ],
                order: 1
            },
            {
                name: '스마트 피아노 - 연주 분석',
                category: '스마트 피아노',
                description: '압력 센서로 피아노 건반을 구현하고, 연주 패턴을 AI가 분석하여 평가하는 프로젝트',
                image:'../assets/images/kits/kit_2.jpeg',
                difficulty: 1,
                duration: 60,
                components: [
                    '아두이노 Uno',
                    '압력 센서 (FSR) x8',
                    '부저 모듈',
                    'LED x8',
                    '저항',
                    '점퍼 와이어'
                ],
                learningObjectives: [
                    '다중 센서 데이터 처리 방법 학습',
                    '소리와 빛을 활용한 피드백 시스템 구현',
                    '음계와 센서 값의 매핑 이해',
                    'AI 기반 연주 패턴 분석'
                ],
                sensors: [
                    { name: '도(C)', type: 'FSR402', pin: 'A0', description: '도 음계 센서' },
                    { name: '레(D)', type: 'FSR402', pin: 'A1', description: '레 음계 센서' },
                    { name: '미(E)', type: 'FSR402', pin: 'A2', description: '미 음계 센서' }
                ],
                order: 2
            },
            {
                name: '스마트 쿠션 - 자세 교정',
                category: '압력 쿠션',
                description: '의자에 앉은 자세를 압력 센서로 감지하고, AI가 올바른 자세인지 판단하여 알림을 주는 프로젝트',
                image:'../assets/images/kits/kit_3.jpeg',
                difficulty: 3,
                duration: 120,
                components: [
                    '아두이노 Mega',
                    '압력 센서 매트릭스 (4x4)',
                    'RGB LED',
                    '부저',
                    'OLED 디스플레이',
                    '쿠션 케이스'
                ],
                learningObjectives: [
                    '센서 매트릭스를 활용한 2D 압력 맵 생성',
                    '복합 센서 데이터의 패턴 인식',
                    '자세 교정 알고리즘 설계',
                    '실시간 피드백 시스템 구현'
                ],
                sensors: [
                    { name: '압력 매트릭스', type: 'FSR Matrix 4x4', pin: 'A0-A15', description: '16개 압력 포인트' }
                ],
                order: 3
            }
        ]);

        console.log('✅ Test kits created:');
        kits.forEach(kit => {
            console.log(`   - ${kit.name} (${kit.category})`);
        });

        process.exit(0);
    } catch (error) {
        console.error('❌ Seed error:', error);
        process.exit(1);
    }
};

seedKits();