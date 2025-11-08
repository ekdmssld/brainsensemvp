// controllers/aiController.js - AI 관련 로직
import OllamaService from '../services/ollamaService.js';
import Kit from '../models/Kit.js';

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