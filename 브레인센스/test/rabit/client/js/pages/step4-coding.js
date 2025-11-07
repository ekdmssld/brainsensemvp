// step4-coding.js - 코딩 페이지 로직
import { API } from '../utils/api.js';
import { Auth } from '../utils/auth.js';
import { Storage } from '../utils/storage.js';
import { showAlert } from '../utils/dom.js';

class CodingPage {
    constructor() {
        this.selectedKit = Storage.getItem('selectedKit');
        this.projectId = Storage.getItem('projectId');
        this.questions = [];
        this.answers = [];
        this.generatedCode = '';

        this.questionsContainer = document.getElementById('questions-container');
        this.codeDisplay = document.getElementById('code-display');
        this.generateBtn = document.getElementById('generate-code-btn');
        this.copyBtn = document.getElementById('copy-code-btn');
        this.backBtn = document.getElementById('back-btn');
        this.nextBtn = document.getElementById('next-btn');
        this.logoutBtn = document.getElementById('logout-btn');
        this.codeLoading = document.getElementById('code-loading');

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
        await this.checkOllamaStatus();
        this.generateQuestions();
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

    async checkOllamaStatus() {
        try {
            const response = await API.get('/ai/health');
            const statusEl = document.getElementById('ollama-status');

            if (response.ollama.running) {
                statusEl.className = 'bg-green-50 border border-green-200 p-4 rounded-lg mb-6';
                statusEl.innerHTML = `
                    <div class="flex items-center">
                        <span class="text-green-600 mr-2">✓</span>
                        <span class="text-green-800">Ollama AI 서버 연결됨 (모델: ${response.ollama.models.join(', ')})</span>
                    </div>
                `;
            } else {
                statusEl.className = 'bg-red-50 border border-red-200 p-4 rounded-lg mb-6';
                statusEl.innerHTML = `
                    <div class="flex items-start">
                        <span class="text-red-600 mr-2">✕</span>
                        <div class="text-red-800">
                            <p class="font-semibold">Ollama 서버가 실행되지 않았습니다</p>
                            <p class="text-sm mt-1">터미널에서 <code class="bg-red-100 px-2 py-1 rounded">ollama serve</code>를 실행해주세요</p>
                        </div>
                    </div>
                `;
                this.generateBtn.disabled = true;
            }
            statusEl.classList.remove('hidden');
        } catch (error) {
            console.error('Ollama status check error:', error);
        }
    }

    generateQuestions() {
        // 키트에 맞는 맞춤형 질문 생성
        this.questions = [
            {
                id: 1,
                question: `${this.selectedKit.sensors?.[0]?.name || '센서'}를 사용하시겠습니까?`,
                type: 'yesno',
                options: ['예', '아니오']
            },
            {
                id: 2,
                question: '센서 데이터를 어떻게 읽을까요?',
                type: 'choice',
                options: [
                    'analogRead()로 아날로그 값 읽기',
                    'digitalRead()로 디지털 값 읽기',
                    'pulseIn()으로 펄스 측정',
                    '라이브러리 함수 사용'
                ]
            },
            {
                id: 3,
                question: '센서 데이터를 시리얼 모니터로 출력하시겠습니까?',
                type: 'yesno',
                options: ['예', '아니오']
            },
            {
                id: 4,
                question: '데이터 샘플링 간격은?',
                type: 'choice',
                options: [
                    '100ms (빠름)',
                    '500ms (보통)',
                    '1000ms (느림)',
                    '사용자 정의'
                ]
            },
            {
                id: 5,
                question: 'LED나 부저로 피드백을 제공하시겠습니까?',
                type: 'yesno',
                options: ['예', '아니오']
            }
        ];

        this.renderQuestions();
    }

    renderQuestions() {
        this.questionsContainer.innerHTML = '';

        this.questions.forEach((q, index) => {
            const card = document.createElement('div');
            card.className = 'question-card border-2 border-gray-200 rounded-lg p-4';
            card.dataset.questionId = q.id;

            let optionsHtml = '';
            if (q.type === 'yesno') {
                optionsHtml = q.options.map(opt => `
                    <button class="option-btn flex-1 px-4 py-2 border-2 border-gray-300 rounded-lg hover:border-indigo-500 hover:bg-indigo-50 transition" data-value="${opt}">
                        ${opt}
                    </button>
                `).join('');
            } else {
                optionsHtml = q.options.map((opt, i) => `
                    <button class="option-btn w-full px-4 py-2 border-2 border-gray-300 rounded-lg text-left hover:border-indigo-500 hover:bg-indigo-50 transition" data-value="${opt}">
                        ${i + 1}. ${opt}
                    </button>
                `).join('');
            }

            card.innerHTML = `
                <div class="flex items-start mb-3">
                    <span class="flex-shrink-0 w-6 h-6 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-sm font-semibold mr-3">
                        ${index + 1}
                    </span>
                    <p class="text-gray-800 font-medium">${q.question}</p>
                </div>
                <div class="ml-9 space-y-2 ${q.type === 'yesno' ? 'flex gap-2' : ''}">
                    ${optionsHtml}
                </div>
            `;

            // 옵션 버튼 이벤트
            card.querySelectorAll('.option-btn').forEach(btn => {
                btn.addEventListener('click', (e) => this.selectAnswer(q.id, e.target.dataset.value, card));
            });

            this.questionsContainer.appendChild(card);
        });
    }

    selectAnswer(questionId, answer, card) {
        // 기존 선택 제거
        card.querySelectorAll('.option-btn').forEach(btn => {
            btn.classList.remove('border-indigo-500', 'bg-indigo-50', 'text-indigo-700', 'font-semibold');
            btn.classList.add('border-gray-300');
        });

        // 새 선택 표시
        event.target.classList.add('border-indigo-500', 'bg-indigo-50', 'text-indigo-700', 'font-semibold');
        event.target.classList.remove('border-gray-300');

        card.classList.add('active');

        // 답변 저장
        const existingIndex = this.answers.findIndex(a => a.questionId === questionId);
        const answerObj = {
            questionId,
            question: this.questions.find(q => q.id === questionId).question,
            answer
        };

        if (existingIndex >= 0) {
            this.answers[existingIndex] = answerObj;
        } else {
            this.answers.push(answerObj);
        }

        // 모든 질문에 답변했는지 확인
        this.updateGenerateButton();
    }

    updateGenerateButton() {
        const allAnswered = this.answers.length === this.questions.length;
        this.generateBtn.disabled = !allAnswered;

        if (allAnswered) {
            this.generateBtn.classList.add('animate-pulse');
        } else {
            this.generateBtn.classList.remove('animate-pulse');
        }
    }

    setupEventListeners() {
        this.generateBtn.addEventListener('click', () => this.generateCode());
        this.copyBtn.addEventListener('click', () => this.copyCode());
        this.backBtn.addEventListener('click', () => window.location.href = 'step2-brainstorming.html');
        this.nextBtn.addEventListener('click', () => window.location.href = 'step5-ai-tutor.html');
        this.logoutBtn.addEventListener('click', () => Auth.logout());
    }

    async generateCode() {
        try {
            // 로딩 표시
            this.codeLoading.classList.remove('hidden');
            this.codeLoading.parentElement.style.position = 'relative';
            this.generateBtn.disabled = true;
            this.generateBtn.textContent = 'AI가 코드를 생성중...';

            // API 호출
            const response = await API.post('/ai/generate-code', {
                questions: this.answers,
                kitId: this.selectedKit._id,
                projectId: this.projectId
            });

            this.generatedCode = response.code;

            // 코드 표시
            this.displayCode(this.generatedCode);

            // 코드 저장
            Storage.setItem('generatedCode', this.generatedCode);

            showAlert('success', '코드가 성공적으로 생성되었습니다!', 'alert-message');

            // 다음 버튼 표시
            this.nextBtn.classList.remove('hidden');
            this.copyBtn.classList.remove('hidden');

        } catch (error) {
            console.error('Generate code error:', error);
            showAlert('error', error.message || '코드 생성 중 오류가 발생했습니다', 'alert-message');
        } finally {
            this.codeLoading.classList.add('hidden');
            this.generateBtn.disabled = false;
            this.generateBtn.textContent = '🤖 AI 코드 재생성하기';
        }
    }

    displayCode(code) {
        // 코드에서 백틱 제거
        const cleanCode = code.replace(/```cpp\n?/g, '').replace(/```\n?/g, '').trim();

        // 코드 하이라이팅 (간단한 버전)
        const highlighted = this.highlightCode(cleanCode);

        this.codeDisplay.innerHTML = `<pre><code>${highlighted}</code></pre>`;
    }

    highlightCode(code) {
        // 간단한 C++ 구문 강조
        return code
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/(\/\/.*$)/gm, '<span style="color: #6A9955;">$1</span>')
            .replace(/(#include|#define|void|int|float|const|digitalWrite|analogRead|pinMode|Serial|delay)/g, '<span style="color: #569CD6;">$1</span>')
            .replace(/(".*?")/g, '<span style="color: #CE9178;">$1</span>')
            .replace(/\b(\d+)\b/g, '<span style="color: #B5CEA8;">$1</span>');
    }

    copyCode() {
        const code = this.generatedCode.replace(/```cpp\n?/g, '').replace(/```\n?/g, '').trim();

        navigator.clipboard.writeText(code).then(() => {
            showAlert('success', '코드가 클립보드에 복사되었습니다', 'alert-message');
            this.copyBtn.textContent = '✓ 복사됨';
            setTimeout(() => {
                this.copyBtn.textContent = '📋 복사';
            }, 2000);
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new CodingPage();
});