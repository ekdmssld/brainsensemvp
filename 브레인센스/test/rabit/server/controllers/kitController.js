// controllers/kitController.js - 키트 관련 로직
import Kit from '../models/Kit.js';

// ====== 기존 코드 유지 ======

// @desc    모든 키트 조회
// @route   GET /api/kits
// @access  Private
export const getKits = async (req, res) => {
    try {
        const kits = await Kit.find({ isActive: true }).sort({ order: 1, createdAt: -1 });
        res.status(200).json({ success: true, count: kits.length, kits });
    } catch (error) {
        console.error('Get kits error:', error);
        res.status(500).json({ success: false, message: '키트 목록을 불러오는데 실패했습니다', error: error.message });
    }
};

// @desc    키트 상세 조회
// @route   GET /api/kits/:id
// @access  Private
export const getKitById = async (req, res) => {
    try {
        const kit = await Kit.findById(req.params.id);
        if (!kit) return res.status(404).json({ success: false, message: '키트를 찾을 수 없습니다' });
        res.status(200).json({ success: true, kit });
    } catch (error) {
        console.error('Get kit error:', error);
        res.status(500).json({ success: false, message: '키트 정보를 불러오는데 실패했습니다', error: error.message });
    }
};

// @desc    카테고리별 키트 조회
// @route   GET /api/kits/category/:category
// @access  Private
export const getKitsByCategory = async (req, res) => {
    try {
        const kits = await Kit.find({ category: req.params.category, isActive: true }).sort({ order: 1 });
        res.status(200).json({ success: true, count: kits.length, kits });
    } catch (error) {
        console.error('Get kits by category error:', error);
        res.status(500).json({ success: false, message: '키트 목록을 불러오는데 실패했습니다', error: error.message });
    }
};

// @desc    키트 생성 (관리자)
// @route   POST /api/kits
// @access  Private/Admin
export const createKit = async (req, res) => {
    try {
        const kit = await Kit.create(req.body);
        res.status(201).json({ success: true, message: '키트가 생성되었습니다', kit });
    } catch (error) {
        console.error('Create kit error:', error);
        res.status(500).json({ success: false, message: '키트 생성에 실패했습니다', error: error.message });
    }
};

// ====== 여기서부터 Step3 추가 ======

// 간단 슬러그
const slugify = (s = '') =>
    String(s).normalize('NFKD').replace(/[^\w\s-]/g, '').trim().replace(/\s+/g, '-').toLowerCase();

// 타입 추론
const inferType = (name = '') => {
    const n = name.toLowerCase();
    if (n.includes('mux') || n.includes('4067') || n.includes('ic')) return 'ic';
    if (n.includes('fsr') || n.includes('sensor') || n.includes('압력')) return 'sensor';
    if (n.includes('oled') || n.includes('bluetooth') || n.includes('module') || n.includes('모듈')) return 'module';
    if (n.includes('led') || n.includes('부저') || n.includes('buzzer')) return 'actuator';
    return 'module';
};

const makeImagePaths = (slug) => ({
    thumbnail: `/assets/images/components/${slug}.png`,
    gallery: [
        `/assets/images/components/gallery/${slug}-1.jpg`,
        `/assets/images/components/gallery/${slug}-2.jpg`,
    ],
});

// Kit 문서 → Step3 카드 리스트
const toComponentCards = (kitDoc) => {
    const list = [];

    (kitDoc.components || []).forEach((label) => {
        const id = slugify(label);
        const type = inferType(label);
        const { thumbnail, gallery } = makeImagePaths(id);
        list.push({
            id,
            type,
            name: label,
            shortDesc: '클릭하여 상세 이미지/설명을 확인하세요.',
            longDesc: '',
            thumbnail,
            images: gallery,
        });
    });

    (kitDoc.sensors || []).forEach((s) => {
        const label = `${s.name} (${s.type})`;
        const id = slugify(label);
        const { thumbnail, gallery } = makeImagePaths(id);
        list.push({
            id,
            type: 'sensor',
            name: label,
            shortDesc: s.description || `Pin: ${s.pin}`,
            longDesc: `Type: ${s.type} | Pin: ${s.pin}${s.description ? ' | ' + s.description : ''}`,
            thumbnail,
            images: gallery,
        });
    });

    // id 중복 제거
    return Array.from(new Map(list.map((x) => [x.id, x])).values());
};

// @desc    (Step3) 키트의 하드웨어 목록
// @route   GET /api/kits/:kitId/components
// @access  Private
export const getKitComponents = async (req, res, next) => {
    try {
        const { kitId } = req.params; // Mongo ObjectId
        const kit = await Kit.findById(kitId);
        if (!kit) return res.status(404).json({ message: '키트를 찾을 수 없습니다.' });
        const components = toComponentCards(kit);
        return res.json({ kitId, components });
    } catch (err) {
        next(err);
    }
};

// @desc    (Step3) 컴포넌트 상세
// @route   GET /api/kits/:kitId/components/:componentId
// @access  Private
export const getComponentDetail = async (req, res, next) => {
    try {
        const { kitId, componentId } = req.params;
        const kit = await Kit.findById(kitId);
        if (!kit) return res.status(404).json({ message: '키트를 찾을 수 없습니다.' });
        const components = toComponentCards(kit);
        const comp = components.find((c) => c.id === componentId);
        if (!comp) return res.status(404).json({ message: '구성 요소를 찾을 수 없습니다.' });
        return res.json(comp);
    } catch (err) {
        next(err);
    }
};