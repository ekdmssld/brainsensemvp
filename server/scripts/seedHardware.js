// server/scripts/seedHardware.js
import mongoose from 'mongoose';
import dotenv from 'dotenv';
import Kit from '../models/Kit.js';

dotenv.config();

const HARDWARE_PRESET = {
    // 키트 이름에 따라 다른 묶음을 적용하고 싶으면 분기 추가하세요.
    default: {
        components: [
            'FSR 압력센서 (10mm)',
            'LED (Red)',
            'CD74HC4067 멀티플렉서',
            '저항 10kΩ',
            '브레드보드',
            '점퍼 와이어',
        ],
        sensors: [
            { name: '앞쪽 압력센서',  type: 'FSR402', pin: 'A0', description: '앞발 압력 측정' },
            { name: '뒤쪽 압력센서',  type: 'FSR402', pin: 'A1', description: '뒷발 압력 측정' },
        ],
    },

    '스마트 인솔 - 보행 분석': {
        components: [
            'FSR 압력센서 (10mm)',
            'LED (Red)',
            'CD74HC4067 멀티플렉서',
            '저항 10kΩ',
            '블루투스 모듈 (HC-06)',
            '배터리 홀더',
        ],
        sensors: [
            { name: '발 앞쪽 센서', type: 'FSR402', pin: 'A0', description: '전족부 압력' },
            { name: '발 뒤쪽 센서', type: 'FSR402', pin: 'A1', description: '후족부 압력' },
        ],
    },

    '스마트 피아노 - 연주 분석': {
        components: [
            'FSR 압력센서 (10mm) x8',
            'LED x8',
            '부저 모듈',
            '저항 220Ω',
            '점퍼 와이어',
        ],
        sensors: [
            { name: '도(C)', type: 'FSR402', pin: 'A0', description: '도 음계 센서' },
            { name: '레(D)', type: 'FSR402', pin: 'A1', description: '레 음계 센서' },
            { name: '미(E)', type: 'FSR402', pin: 'A2', description: '미 음계 센서' },
        ],
    },
};

async function run() {
    try {
        if (!process.env.MONGODB_URI) {
            throw new Error('MONGODB_URI가 .env에 없습니다.');
        }
        await mongoose.connect(process.env.MONGODB_URI);
        console.log('✅ MongoDB Connected');

        const kits = await Kit.find({});
        if (!kits.length) {
            console.log('⚠️  Kit 컬렉션이 비어있습니다. 먼저 seedKits.js를 실행하세요.');
            process.exit(0);
        }

        for (const kit of kits) {
            const preset = HARDWARE_PRESET[kit.name] || HARDWARE_PRESET.default;

            // 기존 값과 합치되, 중복 제거
            const mergedComponents = Array.from(new Set([...(kit.components || []), ...preset.components]));
            const mergedSensors = dedupeSensors([...(kit.sensors || []), ...preset.sensors]);

            kit.components = mergedComponents;
            kit.sensors = mergedSensors;

            await kit.save();
            console.log(`✨ Updated kit: ${kit.name} (${kit._id})`);
        }

        console.log('✅ Hardware seed completed.');
        process.exit(0);
    } catch (err) {
        console.error('❌ Seed hardware error:', err);
        process.exit(1);
    }
}

function dedupeSensors(arr) {
    const sig = (s) => `${s.name}|${s.type}|${s.pin}`;
    const map = new Map();
    for (const s of arr) map.set(sig(s), s);
    return Array.from(map.values());
}

run();