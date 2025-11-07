// services/ollamaService.js - Ollama AI 서비스
import fetch from 'node-fetch';

const OLLAMA_BASE_URL = process.env.OLLAMA_URL || 'http://localhost:11434';
const DEFAULT_MODEL = process.env.OLLAMA_MODEL || 'codellama:7b';

class OllamaService {
    /**
     * Ollama에 프롬프트 전송하고 응답 받기
     */
    static async generate(prompt, options = {}) {
        try {
            const response = await fetch(`${OLLAMA_BASE_URL}/api/generate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    model: options.model || DEFAULT_MODEL,
                    prompt: prompt,
                    stream: false,
                    options: {
                        temperature: options.temperature || 0.7,
                        top_p: options.top_p || 0.9,
                        max_tokens: options.max_tokens || 2000,
                    }
                })
            });

            if (!response.ok) {
                throw new Error(`Ollama API 에러: ${response.status}`);
            }

            const data = await response.json();
            return data.response;
        } catch (error) {
            console.error('Ollama generate error:', error);
            throw new Error('AI 코드 생성 중 오류가 발생했습니다: ' + error.message);
        }
    }

    /**
     * 아두이노 코드 생성
     */
    static async generateArduinoCode(userAnswers, kitInfo) {
        const prompt = this.buildArduinoPrompt(userAnswers, kitInfo);
        return await this.generate(prompt, {
            temperature: 0.3, // 코드 생성은 낮은 temperature
            max_tokens: 1500
        });
    }

    /**
     * 코드 설명 생성
     */
    static async explainCode(code) {
        const prompt = `다음 아두이노 코드를 한국어로 자세히 설명해주세요. 각 줄이 무엇을 하는지, 왜 필요한지 초보자도 이해할 수 있게 설명해주세요:

\`\`\`cpp
${code}
\`\`\`

설명:`;

        return await this.generate(prompt, {
            temperature: 0.5,
            max_tokens: 1000
        });
    }

    /**
     * 아두이노 코드 생성 프롬프트 구성
     */
    static buildArduinoPrompt(userAnswers, kitInfo) {
        const { sensors, purpose, expectedOutput } = userAnswers;

        return `You are an expert Arduino programmer. Generate Arduino C++ code based on the following requirements:

**Kit Information:**
- Kit Name: ${kitInfo.name}
- Sensors: ${kitInfo.sensors?.map(s => `${s.name} (${s.type}) on pin ${s.pin}`).join(', ')}

**User Requirements:**
1. Sensors to use: ${sensors}
2. Purpose: ${purpose}
3. Expected output: ${expectedOutput}

**Code Requirements:**
- Write complete, working Arduino code
- Include all necessary #include statements
- Add clear comments in English
- Include setup() and loop() functions
- Use proper pin definitions
- Add serial output for debugging

Generate only the Arduino code without any additional explanations:

\`\`\`cpp`;
    }

    /**
     * 질문 기반 코드 생성 (O/X, 5지선다 방식)
     */
    static async generateCodeFromQuestions(questions, kitInfo) {
        let codeRequirements = '';

        questions.forEach((q, index) => {
            codeRequirements += `${index + 1}. ${q.question}: ${q.answer}\n`;
        });

        const prompt = `You are an expert Arduino programmer. Generate Arduino C++ code based on these requirements:

**Kit:** ${kitInfo.name}
**Available Sensors:** ${kitInfo.sensors?.map(s => `${s.name} (${s.type}) on pin ${s.pin}`).join(', ')}

**Requirements from user:**
${codeRequirements}

Generate complete, working Arduino code with comments. Output only the code:

\`\`\`cpp`;

        return await this.generate(prompt, {
            temperature: 0.2,
            max_tokens: 2000
        });
    }

    /**
     * Ollama 서버 상태 확인
     */
    static async checkHealth() {
        try {
            const response = await fetch(`${OLLAMA_BASE_URL}/api/tags`);
            return response.ok;
        } catch (error) {
            return false;
        }
    }

    /**
     * 사용 가능한 모델 목록
     */
    static async getModels() {
        try {
            const response = await fetch(`${OLLAMA_BASE_URL}/api/tags`);
            const data = await response.json();
            return data.models || [];
        } catch (error) {
            console.error('Get models error:', error);
            return [];
        }
    }
}


export async function checkHealth() {
    try {
        const r = await fetch(`${OLLAMA_BASE_URL}/api/tags`);
        return r.ok;
    } catch {
        return false;
    }
}

export async function getModels() {
    try {
        const r = await fetch(`${OLLAMA_BASE_URL}/api/tags`);
        if (!r.ok) return [];
        const j = await r.json();
        return (j.models || []).map(m => m.name);
    } catch {
        return [];
    }
}

export default OllamaService;