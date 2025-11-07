// controllers/aiController.js - AI 관련 로직 (정리 버전)
import * as Ollama from '../services/ollamaService.js';
import Kit from '../models/Kit.js';
import Project from '../models/Project.js';

// @desc    질문 기반 코드 생성
// @route   POST /api/ai/generate-code
// @access  Private
export const generateCode = async (req, res) => {
    try {
        const { questions, kitId, projectId } = req.body;

        // 키트 정보 조회
        const kit = await Kit.findById(kitId);
        if (!kit) {
            return res.status(404).json({
                success: false,
                message: '키트를 찾을 수 없습니다',
            });
        }

        // Ollama 서버 상태 확인
        const isOllamaRunning = await Ollama.checkHealth();
        if (!isOllamaRunning) {
            return res.status(503).json({
                success: false,
                message: 'Ollama 서버가 실행되지 않았습니다. 터미널에서 `ollama serve`를 실행해주세요.',
            });
        }

        // 코드 생성
        const generatedCode = await Ollama.generateCodeFromQuestions(questions, kit);

        // 프로젝트에 저장 (선택)
        if (projectId) {
            await Project.findByIdAndUpdate(projectId, {
                generatedCode,
                questions,
                status: 'coding',
                updatedAt: new Date(),
            });
        }

        return res.status(200).json({
            success: true,
            code: generatedCode,
            message: '코드가 성공적으로 생성되었습니다',
        });
    } catch (error) {
        console.error('Generate code error:', error);
        return res.status(500).json({
            success: false,
            message: error?.message || '코드 생성 중 오류가 발생했습니다',
        });
    }
};

// @desc    코드 설명 생성
// @route   POST /api/ai/explain-code
// @access  Private
export const explainCode = async (req, res) => {
    try {
        const { code } = req.body;
        if (!code) {
            return res.status(400).json({
                success: false,
                message: '설명할 코드를 입력해주세요',
            });
        }

        const explanation = await Ollama.explainCode(code);

        return res.status(200).json({
            success: true,
            explanation,
        });
    } catch (error) {
        console.error('Explain code error:', error);
        return res.status(500).json({
            success: false,
            message: '코드 설명 생성 중 오류가 발생했습니다',
        });
    }
};

// @desc    Ollama 상태 확인
// @route   GET /api/ai/health
// @access  Private
export const checkAIHealth = async (req, res) => {
    try {
        const running = await Ollama.checkHealth();
        const models = running ? await Ollama.getModels() : [];

        // 헬스체크는 항상 200으로 응답 (프론트가 배너/UI만 바꿔줌)
        return res.status(200).json({
            success: true,
            ollama: { running, models },
        });
    } catch (error) {
        // 오류여도 200으로 내려서 UI가 “미실행” 배너만 띄우게
        return res.status(200).json({
            success: true,
            ollama: { running: false, models: [] },
        });
    }
};