// controllers/aiController.js - AI 관련 로직
import OllamaService from '../services/ollamaService.js';
import Kit from '../models/Kit.js';


// 파일 상단 근처에 전역 Map 하나 준비 (서버 재시작 시 사라짐)
const AI_MODELS = new Map(); // modelId -> { rules, metrics }

// // 기존 trainRules 수정 X, 아래 후처리만 추가해도 됨
// export const trainRules = async (req, res) => {
//     try {
//         const { sit = [], stand = [], walk = [] } = req.body || {};
//         const model = await OllamaService.induceRulesFromValues({ sit, stand, walk });
//         // model 예시 기대: { rules:[...], metrics:{trainAcc, testAcc, cm}, explain:"..." }
//
//         // modelId 발급해서 저장
//         const modelId = crypto.randomUUID?.() || Date.now().toString();
//         AI_MODELS.set(modelId, model);
//
//         return res.json({ success: true, modelId, ...model });
//     } catch (e) {
//         console.error('[trainRules] error:', e);
//         return res.status(500).json({ success: false, message: e.message || 'server error' });
//     }
// };

// 학습 결과 조회
export const getModel = async (req, res) => {
    const m = AI_MODELS.get(req.params.id);
    if (!m) return res.status(404).json({ success:false, message:'model not found' });
    return res.json({ success:true, modelId: req.params.id, ...m });
};

// 단일 값 예측 (rules 기반 간단 평가; 실제 로직은 서비스에 맞게 변경)
export const predictValue = async (req, res) => {
    const m = AI_MODELS.get(req.params.id);
    if (!m) return res.status(404).json({ success:false, message:'model not found' });

    const v = Number(req.body?.value);
    if (Number.isNaN(v)) return res.status(400).json({ success:false, message:'invalid value' });

    // ① Ollama에 룰/프롬프트로 질의해서 받아오는 방식이 정석이면 이렇게:
    // const out = await OllamaService.predictByRules(m.rules, v);

    // ② 일단 데모용 규칙(임계치) 예시 — 실제로는 위 OllamaService 호출 추천
    let label = '서있음';
    if (v >= 600) label = '앉아있음';
    else if (v >= 400) label = '걷는중';
    const confidence = 0.9; // 데모 값

    return res.json({ success:true, label, confidence });
};
// @desc    단일 질문에 대한 코드 라인 생성
// @route   POST /api/ai/generate-code-line
// @access  Private
export const generateCodeLine = async (req, res) => {
    try {
        const { question, answer, previousCode } = req.body;

        // Ollama 서버 확인
        const isRunning = await OllamaService.checkHealth();
        if (!isRunning) {
            return res.status(503).json({
                success: false,
                message: 'Ollama 서버가 실행되지 않았습니다'
            });
        }

        // 코드 라인 생성
        const codeLine = await OllamaService.generateCodeLine(
            question,
            answer,
            { previousCode }
        );

        // 코드 설명 생성
        const explanation = await OllamaService.explainCodeLine(codeLine);

        res.status(200).json({
            success: true,
            code: codeLine,
            explanation
        });
    } catch (error) {
        console.error('Generate code line error:', error);
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
};

// @desc    전체 코드 완성
// @route   POST /api/ai/finalize-code
// @access  Private
export const finalizeCode = async (req, res) => {
    try {
        const { codeLines, kitId } = req.body;

        const kit = await Kit.findById(kitId);
        if (!kit) {
            return res.status(404).json({
                success: false,
                message: '키트를 찾을 수 없습니다'
            });
        }

        // 전체 코드 생성
        const finalCode = await OllamaService.finalizeCode(codeLines, kit);

        res.status(200).json({
            success: true,
            code: finalCode
        });
    } catch (error) {
        console.error('Finalize code error:', error);
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
};

// @desc    코드 설명 생성
// @route   POST /api/ai/explain-code
// @access  Private
export const explainCode = async (req, res) => {
    try {
        const { code } = req.body;

        const explanation = await OllamaService.explainCodeLine(code);

        res.status(200).json({
            success: true,
            explanation
        });
    } catch (error) {
        console.error('Explain code error:', error);
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
};

// @desc    Ollama 상태 확인
// @route   GET /api/ai/health
// @access  Private
export const checkAIHealth = async (req, res) => {
    try {
        const isHealthy = await OllamaService.checkHealth();
        const models = isHealthy ? await OllamaService.getModels() : [];

        res.status(200).json({
            success: true,
            ollama: {
                running: isHealthy,
                models: models.map(m => m.name),
                url: process.env.OLLAMA_URL || 'http://localhost:11434'
            }
        });
    } catch (error) {
        res.status(200).json({
            success: true,
            ollama: {
                running: false,
                models: []
            }
        });
    }
};

export const trainRules = async (req, res) => {
    try {
        const { sit = [], stand = [], walk = [] } = req.body || {};
        const model = await OllamaService.induceRulesFromValues({ sit, stand, walk });
        return res.json({ success: true, model });
    } catch (e) {
        console.error('[trainRules] error:', e);
        return res.status(500).json({ success: false, message: e.message || 'server error' });
    }
};