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
    // 파일 하단 어딘가 (class OllamaService 내부)에 추가
    static async induceRulesFromValues({ sit = [], stand = [], walk = [] }) {
        const prompt = `
너는 초등·중학생도 이해할 수 있는 간단한 규칙 기반 분류기를 만드는 도우미야.
아래는 압력센서 값(정수) 데이터셋이야. 각 라벨의 값 범위를 관찰해서
사람이 이해할 수 있는 간단한 if-else 규칙을 JSON으로 만들어줘.

[앉아있음] ${sit.join(', ')}
[서있음]   ${stand.join(', ')}
[걷는중]   ${walk.join(', ')}

요구사항:
1) "rules"는 위에서 아래로 평가되는 if-else 체인으로 작성
2) 각 rule의 "condition"에는 value만 사용 (예: "value >= 600 && value < 800")
3) 라벨은 "앉아있음", "서있음", "걷는중" 중 하나
4) 마지막에는 어떤 값에도 매칭되는 "else" 규칙을 하나 포함
5) "reasoning"에는 경계값을 어떻게 정했는지 한국어로 2~3문장 설명

반환 JSON 예:
{
  "rules":[
    {"condition":"value >= 600","label":"앉아있음"},
    {"condition":"value >= 400 && value < 600","label":"걷는중"},
    {"condition":"else","label":"서있음"}
  ],
  "reasoning":"앉아있음은 대체로 600 이상, 걷기는 400~600 사이, 나머지는 서있음으로 구분했습니다."
}
`;

        const raw = await this.generate(prompt, { temperature: 0.2, num_predict: 180 });
        // 모델이 코드블록 등을 섞어 보낼 수도 있어 단순 정제
        const match = raw.match(/\{[\s\S]*\}/);
        const jsonStr = match ? match[0] : raw;
        try {
            const parsed = JSON.parse(jsonStr);
            // 간단 검증
            if (Array.isArray(parsed.rules) && parsed.reasoning) return parsed;
            return { rules: [], reasoning: "규칙 생성 실패(형식 오류)" };
        } catch(e){
            return { rules: [], reasoning: "규칙 생성 실패(JSON 파싱 실패)" };
        }
    }
}



export default OllamaService;