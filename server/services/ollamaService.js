// services/ollamaService.js - Ollama AI 서비스
const OLLAMA_BASE_URL = process.env.OLLAMA_URL || 'http://localhost:11434';
const DEFAULT_MODEL = process.env.OLLAMA_MODEL || 'codellama:7b';

class OllamaService {
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

    /**
     * Ollama에 프롬프트 전송
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
                        temperature: options.temperature || 0.2,
                        top_p: options.top_p || 0.9,
                        num_predict: options.num_predict || 100,
                    }
                })
            });

            if (!response.ok) {
                throw new Error(`Ollama API 에러: ${response.status}`);
            }

            const data = await response.json();
            return data.response.trim();
        } catch (error) {
            console.error('Ollama generate error:', error);
            throw new Error('AI 코드 생성 중 오류가 발생했습니다: ' + error.message);
        }
    }

    /**
     * 단일 질문에 대한 코드 라인 생성 (1-2줄)
     */
    static async generateCodeLine(question, answer, context = {}) {
        const prompt = `You are an Arduino code generator. Generate ONLY 1-2 lines of Arduino C++ code based on this requirement.

Question: ${question}
Answer: ${answer}

${context.previousCode ? `Previous code context:\n${context.previousCode}\n` : ''}

Generate ONLY the Arduino code lines needed for this specific requirement. No explanations, no comments, just the code.
If this is about setup, generate setup() code.
If this is about sensor reading, generate analogRead() or digitalRead() code.
If this is about output, generate digitalWrite() or analogWrite() code.

Code:`;

        return await this.generate(prompt, {
            temperature: 0.1,
            num_predict: 32
        });
    }

    /**
     * 전체 코드 정리 및 완성
     */
    static async finalizeCode(codeLines, kitInfo) {
        const prompt = `Given these Arduino code snippets, create a complete, properly formatted Arduino program.

Kit: ${kitInfo.name}
Sensors: ${kitInfo.sensors?.map(s => `${s.name} on pin ${s.pin}`).join(', ')}

Code snippets:
${codeLines.join('\n')}

Create a complete Arduino program with:
1. Necessary #include statements
2. Pin definitions at the top
3. Proper setup() function
4. Proper loop() function
5. Brief comments in English

Output ONLY the complete Arduino code:

\`\`\`cpp`;

        const fullCode = await this.generate(prompt, {
            temperature: 0.2,
            num_predict: 500
        });

        // 코드 블록 마커 제거
        return fullCode.replace(/```cpp\n?/g, '').replace(/```\n?/g, '').trim();
    }

    /**
     * 코드 라인 설명 생성
     */
    static async explainCodeLine(code) {
        const prompt = `Explain this Arduino code line in Korean, in one simple sentence for beginners:

${code}

Korean explanation:`;

        return await this.generate(prompt, {
            temperature: 0.4,
            num_predict: 40
        });
    }
}

export default OllamaService;