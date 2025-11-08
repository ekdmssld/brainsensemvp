// step4-coding.js
import { API } from '../utils/api.js';
import { Auth } from '../utils/auth.js';
import { Storage } from '../utils/storage.js';
import { showAlert } from '../utils/dom.js';

class CodingPage {
    constructor() {
        this.selectedKit = Storage.getItem('selectedKit');
        this.selectedComponents = Storage.getItem('selectedComponents') || [];
        this.projectId = Storage.getItem('projectId');

        this.questions = [];
        this.currentQuestionIndex = 0;
        this.answers = [];
        this.codeLines = [];
        this.explanations = []; // AI 설명 저장
        this.isOllamaRunning = false;

        // DOM elements
        this.questionContainer = document.getElementById('question-container');
        this.codeDisplay = document.getElementById('code-display');
        this.progressBar = document.getElementById('progress-bar');
        this.currentQuestionSpan = document.getElementById('current-question');
        this.totalQuestionsSpan = document.getElementById('total-questions');
        this.prevBtn = document.getElementById('prev-question-btn');
        this.nextBtn = document.getElementById('next-question-btn');
        this.finishBtn = document.getElementById('finish-btn');
        this.copyBtn = document.getElementById('copy-code-btn');
        this.uploadBtn = document.getElementById('upload-btn');
        this.backBtn = document.getElementById('back-btn');
        this.nextStepBtn = document.getElementById('next-step-btn');
        this.aiThinking = document.getElementById('ai-thinking');
        this.aiTutorBubble = document.getElementById('ai-tutor-bubble');
        this.aiTutorText = document.getElementById('ai-tutor-text');

        this.init();
    }

    async init() {
        if (!Auth.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }

        if (!this.selectedKit) {
            showAlert('error', '키트를 먼저 선택해주세요', 'alert-message');
            setTimeout(() => window.location.href = 'step1-kit-selection.html', 2000);
            return;
        }

        this.displayUserInfo();
        this.displayKitName();

        await this.checkOllama();
        this.generateQuestions();
        this.showCurrentQuestion();
        this.setupEventListeners();
    }

    displayUserInfo() {
        const user = Auth.getUser();
        if (user) {
            document.getElementById('user-name').textContent = user.name;
        }
    }

    displayKitName() {
        document.getElementById('kit-name').textContent = this.selectedKit.name;
    }

    async checkOllama() {
        try {
            const response = await API.get('/ai/health');
            this.isOllamaRunning = response.ollama.running;

            const statusEl = document.getElementById('ollama-status');

            if (this.isOllamaRunning) {
                statusEl.className = 'bg-green-50 border border-green-200 p-4 rounded-lg mb-6';
                statusEl.innerHTML = `
                    <div class="flex items-center">
                        <span class="text-green-600 mr-2">✓</span>
                        <span class="text-green-800 text-sm">
                            Ollama AI 연결됨 (${response.ollama.models.join(', ')})
                        </span>
                    </div>
                `;
            } else {
                statusEl.className = 'bg-red-50 border border-red-200 p-4 rounded-lg mb-6';
                statusEl.innerHTML = `
                    <div class="flex items-start">
                        <span class="text-red-600 mr-2">✕</span>
                        <div class="text-red-800 text-sm">
                            <p class="font-semibold">Ollama 서버가 실행되지 않았습니다</p>
                            <p class="mt-1">터미널에서 <code class="bg-red-100 px-2 py-1 rounded">ollama serve</code>를 실행해주세요</p>
                        </div>
                    </div>
                `;
            }
            statusEl.classList.remove('hidden');
        } catch (error) {
            console.error('Ollama check error:', error);
        }
    }

    generateQuestions() {
        const sensor = this.selectedComponents.find(c => c.category === 'sensor');

        this.questions = [
            {
                id: 1,
                text: `${sensor?.name || '센서'}를 어느 핀에 연결하시겠습니까?`,
                type: 'text',
                placeholder: 'A0',
                codeHint: 'pin definition'
            },
            {
                id: 2,
                text: '센서 데이터를 읽는 방법은?',
                type: 'choice',
                options: [
                    'analogRead() - 아날로그 값 (0-1023)',
                    'digitalRead() - 디지털 값 (HIGH/LOW)',
                ],
                codeHint: 'sensor read'
            },
            {
                id: 3,
                text: '시리얼 모니터로 데이터를 출력하시겠습니까?',
                type: 'yesno',
                options: ['예', '아니오'],
                codeHint: 'serial output'
            },
            {
                id: 4,
                text: '데이터를 읽는 간격은?',
                type: 'choice',
                options: [
                    '100ms (빠름)',
                    '500ms (보통)',
                    '1000ms (느림)'
                ],
                codeHint: 'delay'
            }
        ];

        this.totalQuestionsSpan.textContent = this.questions.length;
        this.updateProgress();
    }

    showCurrentQuestion() {
        const question = this.questions[this.currentQuestionIndex];
        if (!question) return;

        let html = `
            <div class="mb-4">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-sm font-semibold text-indigo-600">질문 ${this.currentQuestionIndex + 1}</span>
                    <span class="text-xs text-gray-500">${question.codeHint}</span>
                </div>
                <h3 class="text-lg font-bold text-gray-800 mb-4">${question.text}</h3>
            </div>
        `;

        if (question.type === 'text') {
            html += `
                <input 
                    type="text" 
                    id="answer-input"
                    class="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:border-indigo-500 focus:outline-none"
                    placeholder="${question.placeholder}"
                    value="${this.answers[this.currentQuestionIndex]?.answer || ''}"
                >
            `;
        } else if (question.type === 'yesno') {
            html += `
                <div class="flex gap-3">
                    ${question.options.map(opt => `
                        <button class="answer-btn flex-1 px-6 py-4 border-2 border-gray-300 rounded-lg hover:border-indigo-500 hover:bg-indigo-50 transition ${this.answers[this.currentQuestionIndex]?.answer === opt ? 'border-indigo-500 bg-indigo-50 font-semibold' : ''}" data-value="${opt}">
                            ${opt}
                        </button>
                    `).join('')}
                </div>
            `;
        } else if (question.type === 'choice') {
            html += `
                <div class="space-y-2">
                    ${question.options.map((opt, i) => `
                        <button class="answer-btn w-full px-4 py-3 border-2 border-gray-300 rounded-lg text-left hover:border-indigo-500 hover:bg-indigo-50 transition ${this.answers[this.currentQuestionIndex]?.answer === opt ? 'border-indigo-500 bg-indigo-50 font-semibold' : ''}" data-value="${opt}">
                            ${i + 1}. ${opt}
                        </button>
                    `).join('')}
                </div>
            `;
        }

        this.questionContainer.innerHTML = html;

        // 이벤트 리스너
        if (question.type === 'text') {
            const input = document.getElementById('answer-input');
            input.addEventListener('input', (e) => {
                this.saveAnswer(e.target.value);
            });
            input.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && this.answers[this.currentQuestionIndex]) {
                    this.nextQuestion();
                }
            });
        } else {
            document.querySelectorAll('.answer-btn').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    this.saveAnswer(e.currentTarget.dataset.value);
                    this.showCurrentQuestion();
                });
            });
        }

        this.updateNavButtons();

        // 이전 AI 튜터 설명 표시 (있다면)
        if (this.explanations[this.currentQuestionIndex]) {
            this.showAITutor(this.explanations[this.currentQuestionIndex]);
        } else {
            this.hideAITutor();
        }
    }

    saveAnswer(answer) {
        const question = this.questions[this.currentQuestionIndex];

        this.answers[this.currentQuestionIndex] = {
            questionId: question.id,
            question: question.text,
            answer: answer,
            codeHint: question.codeHint
        };

        this.nextBtn.disabled = false;
    }

    async nextQuestion() {
        if (!this.answers[this.currentQuestionIndex]) {
            showAlert('warning', '답변을 선택해주세요', 'alert-message');
            return;
        }

        // ⭐ 다음 버튼을 눌렀을 때 코드 생성
        if (this.isOllamaRunning) {
            const question = this.questions[this.currentQuestionIndex];
            const answer = this.answers[this.currentQuestionIndex].answer;
            await this.generateCodeForAnswer(question, answer);
        }

        if (this.currentQuestionIndex < this.questions.length - 1) {
            this.currentQuestionIndex++;
            this.showCurrentQuestion();
            this.updateProgress();
        } else {
            // 마지막 질문
            this.finishBtn.classList.remove('hidden');
            this.nextBtn.classList.add('hidden');
        }
    }

    async generateCodeForAnswer(question, answer) {
        try {
            this.aiThinking.classList.remove('hidden');

            const previousCode = this.codeLines.filter(line => line).join('\n');

            const response = await API.post('/ai/generate-code-line', {
                question: question.text,
                answer: answer,
                previousCode: previousCode
            });

            const codeLine = response.code;
            const explanation = response.explanation;

            // 코드 라인 저장
            const questionIndex = this.currentQuestionIndex;
            this.codeLines[questionIndex] = codeLine;
            this.explanations[questionIndex] = explanation;

            // 타이핑 효과로 코드 표시
            await this.displayCodeWithTyping(codeLine);

            // AI 튜터 설명 표시
            if (explanation) {
                this.showAITutor(explanation);
            }

        } catch (error) {
            console.error('Generate code error:', error);
            showAlert('error', 'AI 코드 생성 중 오류가 발생했습니다', 'alert-message');
        } finally {
            this.aiThinking.classList.add('hidden');
        }
    }

    showAITutor(explanation) {
        this.aiTutorText.textContent = explanation;
        this.aiTutorBubble.classList.remove('hidden');
    }

    hideAITutor() {
        this.aiTutorBubble.classList.add('hidden');
    }

    displayCode() {
        const fullCode = this.codeLines.filter(line => line).join('\n');

        if (!fullCode) {
            this.codeDisplay.innerHTML = `
                <div class="text-gray-500 text-center py-12">
                    <svg class="w-16 h-16 mx-auto mb-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"></path>
                    </svg>
                    <p>질문에 답변하고 "다음" 버튼을 누르세요</p>
                </div>
            `;
            return;
        }

        const cleanCode = this.cleanCode(fullCode);
        const lines = cleanCode.split('\n');

        // 줄번호 생성 (세로 정렬)
        const lineNumbers = lines.map((_, i) => `<div>${i + 1}</div>`).join('');

        // Prism으로 하이라이팅
        const highlighted = Prism.highlight(cleanCode, Prism.languages.cpp, 'cpp');

        this.codeDisplay.innerHTML = `
            <div class="line-numbers">${lineNumbers}</div>
            <pre class="language-cpp"><code class="language-cpp">${highlighted}</code></pre>
        `;

        this.copyBtn.classList.remove('hidden');
    }

    cleanCode(code) {
        if (!code) return '';

        return code
            .replace(/```cpp\n?/g, '')
            .replace(/```c\+\+\n?/g, '')
            .replace(/```c\n?/g, '')
            .replace(/```\n?/g, '')
            .replace(/<\/?[^>]+(>|$)/g, '')
            .replace(/style="[^"]*"/g, '')
            .replace(/^["'](.*)["']$/gm, '$1')
            .replace(/\n{3,}/g, '\n\n')
            .trim();
    }

    async displayCodeWithTyping(newCodeLine) {
        await this.typeCode(newCodeLine);
        this.displayCode();
    }

    async typeCode(code) {
        const cleanNewCode = this.cleanCode(code);
        const speed = 20;

        let currentText = '';

        for (let i = 0; i < cleanNewCode.length; i++) {
            currentText += cleanNewCode[i];

            const previousCode = this.codeLines.slice(0, -1).filter(line => line).join('\n');
            const fullCode = previousCode ? previousCode + '\n' + currentText : currentText;
            const cleanFullCode = this.cleanCode(fullCode);

            const highlighted = Prism.highlight(cleanFullCode, Prism.languages.cpp, 'cpp');
            const lines = cleanFullCode.split('\n');
            const lineNumbers = lines.map((_, i) => `<div>${i + 1}</div>`).join('');

            this.codeDisplay.innerHTML = `
                <div class="line-numbers">${lineNumbers}</div>
                <pre class="language-cpp"><code class="language-cpp">${highlighted}<span class="typing-cursor"></span></code></pre>
            `;

            this.codeDisplay.scrollTop = this.codeDisplay.scrollHeight;
            await this.sleep(speed);
        }
    }

    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    prevQuestion() {
        if (this.currentQuestionIndex > 0) {
            this.currentQuestionIndex--;
            this.showCurrentQuestion();
            this.updateProgress();
        }
    }

    async finishCode() {
        if (!this.isOllamaRunning) {
            showAlert('error', 'Ollama 서버가 실행되지 않았습니다', 'alert-message');
            return;
        }

        try {
            this.aiThinking.classList.remove('hidden');
            this.finishBtn.disabled = true;
            this.finishBtn.textContent = 'AI가 코드를 정리하는 중...';

            const response = await API.post('/ai/finalize-code', {
                codeLines: this.codeLines,
                kitId: this.selectedKit._id
            });

            const finalCode = response.code;

            this.codeLines = [finalCode];
            this.displayCode();

            Storage.setItem('generatedCode', finalCode);

            showAlert('success', '코드가 완성되었습니다!', 'alert-message');
            this.nextStepBtn.classList.remove('hidden');
            this.uploadBtn.disabled = false;

        } catch (error) {
            console.error('Finalize code error:', error);
            showAlert('error', '코드 완성 중 오류가 발생했습니다', 'alert-message');
        } finally {
            this.aiThinking.classList.add('hidden');
            this.finishBtn.disabled = false;
            this.finishBtn.textContent = '✓ 코드 완성하기';
        }
    }

    updateProgress() {
        const progress = ((this.currentQuestionIndex + 1) / this.questions.length) * 100;
        this.progressBar.style.width = `${progress}%`;
        this.currentQuestionSpan.textContent = this.currentQuestionIndex + 1;
    }

    updateNavButtons() {
        this.prevBtn.disabled = this.currentQuestionIndex === 0;
        this.nextBtn.disabled = !this.answers[this.currentQuestionIndex];
    }

    copyCode() {
        const code = this.cleanCode(this.codeLines.join('\n'));

        navigator.clipboard.writeText(code).then(() => {
            showAlert('success', '코드가 복사되었습니다', 'alert-message');
            this.copyBtn.textContent = '✓ 복사됨';
            setTimeout(() => {
                this.copyBtn.textContent = '📋 복사';
            }, 2000);
        });
    }

    uploadToArduino() {
        showAlert('info', '아두이노 IDE에서 코드를 복사하여 업로드해주세요', 'alert-message');
        this.copyCode();
    }

    setupEventListeners() {
        this.prevBtn.addEventListener('click', () => this.prevQuestion());
        this.nextBtn.addEventListener('click', () => this.nextQuestion());
        this.finishBtn.addEventListener('click', () => this.finishCode());
        this.copyBtn.addEventListener('click', () => this.copyCode());
        this.uploadBtn.addEventListener('click', () => this.uploadToArduino());
        this.backBtn.addEventListener('click', () => window.location.href = 'step3-hardware-design.html');
        this.nextStepBtn.addEventListener('click', () => window.location.href = 'step5-ai-tutor.html');
        document.getElementById('logout-btn').addEventListener('click', () => Auth.logout());
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new CodingPage();
});

// 누적 코드 문자열(또는 배열)
let accumulatedCode = "";

// 문항 제출할 때 호출
async function onSubmitAnswer(stepIndex, questionText, userAnswer) {
    try {
        const res = await fetch('/api/ai/generate-code-line', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                question: questionText,
                answer: userAnswer,
                previousCode: accumulatedCode, // 이미 만든 코드 컨텍스트
            })
        });

        const data = await res.json();
        if (!data.success) throw new Error(data.message || 'AI error');

        // 1) 코드 누적
        const newLine = (data.code || '').trim();
        if (newLine) {
            accumulatedCode += (accumulatedCode ? '\n' : '') + newLine;
            renderCode(accumulatedCode); // 코드 영역 즉시 업데이트
        }

        // 2) AI 튜터 말풍선 즉시 띄우기
        showTutorBubble(data.explanation); // ← “말풍선” 컴포넌트에 텍스트 넣고 표시

        // 3) 다음 문항으로 진행
        goToNextStep(stepIndex + 1);

    } catch (e) {
        console.error(e);
        toast('AI 생성 중 오류가 발생했습니다.');
    }
}

function showTutorBubble(text) {
    const bubble = document.getElementById('ai-tutor-bubble');
    const txt = document.getElementById('ai-tutor-text');
    if (txt) txt.textContent = text || '';
    bubble?.classList.remove('hidden');
}

function hideTutorBubble() {
    document.getElementById('ai-tutor-bubble')?.classList.add('hidden');
}

function onPrevStep() {
    // 코드 되돌림 처리(선택) 또는 유지
    hideTutorBubble(); // 이전 단계로 가면 말풍선은 감추기
    goToPrevStep();
}