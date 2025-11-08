// step5-ai-tutor.js - AI 튜터 페이지
import { API } from '../utils/api.js';
import { Auth } from '../utils/auth.js';
import { Storage } from '../utils/storage.js';
import { showAlert } from '../utils/dom.js';
import { SerialPortManager } from "../utils/serial.js";

class AITutorPage {
    constructor() {
        this.selectedKit = Storage.getItem('selectedKit');
        this.generatedCode = Storage.getItem('generatedCode') || '';
        this.codeLines = [];
        this.currentLineIndex = null;

        // DOM
        this.codeLinesContainer = document.getElementById('code-lines');
        this.explanationContent = document.getElementById('explanation-content');
        this.serialOutput = document.getElementById('serial-output');
        this.copyBtn = document.getElementById('copy-code-btn');
        this.explainAllBtn = document.getElementById('explain-all-btn');
        this.startMonitorBtn = document.getElementById('start-monitor-btn');
        this.backBtn = document.getElementById('back-btn');
        this.nextBtn = document.getElementById('next-btn');
        this.serialManager = new SerialPortManager();
        this.selectedPort = null;

        this.init();
    }

    async init() {
        if (!Auth.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }

        if (!this.generatedCode) {
            showAlert('error', '생성된 코드가 없습니다', 'alert-message');
            setTimeout(() => window.location.href = 'step4-coding.html', 2000);
            return;
        }

        this.displayUserInfo();
        this.displayKitName();
        this.parseAndDisplayCode();
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

    parseAndDisplayCode() {
        // 코드를 라인별로 분리
        const lines = this.generatedCode.split('\n').filter(line => line.trim() !== '');
        this.codeLines = lines;

        // 각 라인을 클릭 가능하게 표시
        this.codeLinesContainer.innerHTML = lines.map((line, index) => {
            const highlighted = Prism.highlight(line, Prism.languages.cpp, 'cpp');
            return `
                <div class="code-line" data-line-index="${index}">
                    <span class="line-number">${index + 1}</span>
                    <code class="language-cpp">${highlighted}</code>
                </div>
            `;
        }).join('');

        // 라인 클릭 이벤트
        document.querySelectorAll('.code-line').forEach(lineEl => {
            lineEl.addEventListener('click', () => {
                const index = parseInt(lineEl.dataset.lineIndex);
                this.explainLine(index);
            });
        });
    }

    async explainLine(index) {
        const line = this.codeLines[index];

        // 활성화 표시
        document.querySelectorAll('.code-line').forEach(el => el.classList.remove('active'));
        document.querySelector(`[data-line-index="${index}"]`).classList.add('active');

        // 로딩 표시
        this.explanationContent.innerHTML = `
            <div class="text-center py-12">
                <div class="w-12 h-12 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                <p class="text-gray-600">AI가 설명을 생성하는 중...</p>
            </div>
        `;

        try {
            // AI에게 코드 설명 요청
            const response = await API.post('/ai/explain-code', {
                code: line
            });

            const explanation = response.explanation;

            // 설명 표시
            this.displayExplanation(line, explanation, index + 1);

        } catch (error) {
            console.error('Explain error:', error);
            this.explanationContent.innerHTML = `
                <div class="bg-red-50 border border-red-200 rounded-lg p-4">
                    <p class="text-red-800">설명을 생성하는 중 오류가 발생했습니다</p>
                </div>
            `;
        }
    }

    displayExplanation(code, explanation, lineNumber) {
        const highlighted = Prism.highlight(code, Prism.languages.cpp, 'cpp');

        this.explanationContent.innerHTML = `
            <div class="explanation-card">
                <!-- Line Number Badge -->
                <div class="inline-flex items-center px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full text-sm font-semibold mb-4">
                    <span>라인 ${lineNumber}</span>
                </div>

                <!-- Code -->
                <div class="bg-gray-900 rounded-lg p-4 mb-4">
                    <pre class="language-cpp" style="margin: 0;"><code class="language-cpp">${highlighted}</code></pre>
                </div>

                <!-- Explanation -->
                <div class="bg-white rounded-lg p-4 shadow-sm">
                    <h4 class="font-bold text-gray-800 mb-2 flex items-center">
                        <svg class="w-5 h-5 text-indigo-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                        </svg>
                        설명
                    </h4>
                    <p class="text-gray-700 leading-relaxed">${explanation}</p>
                </div>

                <!-- Related Concepts -->
                <div class="mt-4 p-4 bg-purple-50 rounded-lg border border-purple-200">
                    <h4 class="font-semibold text-purple-900 mb-2 text-sm">💡 관련 개념</h4>
                    <div id="related-concepts" class="text-sm text-purple-800">
                        ${this.getRelatedConcepts(code)}
                    </div>
                </div>

                <!-- Troubleshooting -->
                ${this.getTroubleshooting(code)}
            </div>
        `;
    }

    getRelatedConcepts(code) {
        if (code.includes('pinMode')) {
            return '• pinMode()은 핀의 동작 모드를 설정합니다 (INPUT, OUTPUT, INPUT_PULLUP)';
        } else if (code.includes('analogRead')) {
            return '• analogRead()는 0-1023 범위의 값을 반환합니다 (10비트 ADC)';
        } else if (code.includes('digitalWrite')) {
            return '• digitalWrite()는 디지털 핀을 HIGH(5V) 또는 LOW(0V)로 설정합니다';
        } else if (code.includes('Serial.begin')) {
            return '• Serial.begin()은 시리얼 통신 속도(보드레이트)를 설정합니다';
        } else if (code.includes('delay')) {
            return '• delay()는 밀리초(ms) 단위로 대기합니다 (1000ms = 1초)';
        }
        return '• 기본적인 아두이노 명령어입니다';
    }

    getTroubleshooting(code) {
        if (code.includes('analogRead') || code.includes('digitalWrite')) {
            return `
                <div class="mt-4 p-4 bg-yellow-50 rounded-lg border border-yellow-200">
                    <h4 class="font-semibold text-yellow-900 mb-2 text-sm">⚠️ 문제 해결</h4>
                    <ul class="text-sm text-yellow-800 space-y-1">
                        <li>• 센서가 제대로 연결되었는지 확인하세요</li>
                        <li>• 핀 번호가 올바른지 확인하세요</li>
                        <li>• 전원이 공급되고 있는지 확인하세요</li>
                    </ul>
                </div>
            `;
        }
        return '';
    }

    async explainAll() {
        this.explanationContent.innerHTML = `
            <div class="text-center py-12">
                <div class="w-12 h-12 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                <p class="text-gray-600">전체 코드 설명을 생성하는 중...</p>
            </div>
        `;

        try {
            const response = await API.post('/ai/explain-code', {
                code: this.generatedCode
            });

            this.explanationContent.innerHTML = `
                <div class="explanation-card">
                    <h4 class="font-bold text-gray-800 mb-4 text-lg">📖 전체 코드 설명</h4>
                    <div class="bg-white rounded-lg p-4 shadow-sm">
                        <p class="text-gray-700 leading-relaxed whitespace-pre-line">${response.explanation}</p>
                    </div>
                </div>
            `;
        } catch (error) {
            console.error('Explain all error:', error);
            showAlert('error', '전체 설명 생성 중 오류가 발생했습니다', 'alert-message');
        }
    }

    startSerialMonitor() {
        // 시뮬레이션 데이터
        const mockData = [
            'FSR Pressure Sensor Test',
            'Sensor pin: A2',
            'Threshold: 600',
            '------------------------',
            'Sensor Value: 0',
            'Sensor Value: 0',
            'Sensor Value: 145',
            'Sensor Value: 389',
            'Sensor Value: 712',
            'Sensor Value: 856'
        ];

        this.serialOutput.innerHTML = '';
        let index = 0;

        const interval = setInterval(() => {
            if (index >= mockData.length) {
                clearInterval(interval);
                return;
            }

            const line = document.createElement('div');
            line.className = 'serial-line';
            line.textContent = mockData[index];
            this.serialOutput.appendChild(line);
            this.serialOutput.scrollTop = this.serialOutput.scrollHeight;

            index++;
        }, 500);

        this.startMonitorBtn.textContent = '⏸ 일시정지';
        this.startMonitorBtn.classList.replace('bg-green-600', 'bg-yellow-600');
    }

    copyCode() {
        navigator.clipboard.writeText(this.generatedCode).then(() => {
            showAlert('success', '코드가 복사되었습니다', 'alert-message');
            this.copyBtn.textContent = '✓ 복사됨';
            setTimeout(() => {
                this.copyBtn.textContent = '📋 복사';
            }, 2000);
        });
    }

    // ⭐ 정리된 setupSettingsModal 메서드
    setupSettingsModal() {
        // DOM 요소 가져오기
        const boardBtn = document.getElementById('board-setting-btn');
        const portBtn = document.getElementById('port-setting-btn');
        const modal = document.getElementById('settings-modal');
        const closeBtn = document.getElementById('close-settings-modal');
        const cancelBtn = document.getElementById('cancel-settings-btn');
        const confirmBtn = document.getElementById('confirm-settings-btn');
        const scanPortsBtn = document.getElementById('scan-ports-btn');
        const portInfo = document.getElementById('port-info');
        const portName = document.getElementById('port-name');
        const serialUnsupported = document.getElementById('serial-unsupported');

        // Web Serial API 지원 확인
        if (!SerialPortManager.isSupported()) {
            serialUnsupported?.classList.remove('hidden');
            if (scanPortsBtn) {
                scanPortsBtn.disabled = true;
                scanPortsBtn.classList.add('opacity-50', 'cursor-not-allowed');
            }
        }

        // 보드 설정 버튼
        if (boardBtn) {
            boardBtn.addEventListener('click', () => {
                document.getElementById('modal-title').textContent = '보드 설정';
                modal.classList.remove('hidden');
            });
        }

        // 포트 설정 버튼
        if (portBtn) {
            portBtn.addEventListener('click', () => {
                document.getElementById('modal-title').textContent = '포트 설정';
                modal.classList.remove('hidden');
            });
        }

        // 모달 닫기
        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                modal.classList.add('hidden');
            });
        }

        // 취소 버튼
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => {
                modal.classList.add('hidden');
            });
        }

        // 포트 검색 버튼
        if (scanPortsBtn) {
            scanPortsBtn.addEventListener('click', async () => {
                scanPortsBtn.disabled = true;
                scanPortsBtn.innerHTML = `
                    <div class="flex items-center justify-center">
                        <div class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                        <span>포트 검색 중...</span>
                    </div>
                `;

                try {
                    const result = await this.serialManager.requestPort();

                    if (result.success) {
                        this.selectedPort = result.port;
                        const info = this.serialManager.getPortInfo();

                        // 포트 정보 표시
                        if (portInfo && portName) {
                            portName.textContent = `포트 선택됨 (VID: ${info.usbVendorId || 'N/A'}, PID: ${info.usbProductId || 'N/A'})`;
                            portInfo.classList.remove('hidden');
                        }

                        showAlert('success', '포트가 선택되었습니다', 'alert-message');
                    } else {
                        showAlert('warning', result.message, 'alert-message');
                    }
                } catch (error) {
                    console.error('Port scan error:', error);
                    showAlert('error', '포트 검색 중 오류가 발생했습니다', 'alert-message');
                } finally {
                    scanPortsBtn.disabled = false;
                    scanPortsBtn.innerHTML = '🔍 포트 검색';
                }
            });
        }

        // 확인 버튼
        if (confirmBtn) {
            confirmBtn.addEventListener('click', () => {
                const board = document.getElementById('board-select')?.value;
                const baudrate = document.getElementById('baudrate-select')?.value;

                const settings = {
                    board,
                    baudrate: parseInt(baudrate),
                    port: this.selectedPort ? 'connected' : null,
                    timestamp: new Date().toISOString()
                };

                Storage.setItem('arduinoSettings', settings);
                showAlert('success', '설정이 저장되었습니다', 'alert-message');
                modal.classList.add('hidden');
            });
        }
    }

    setupEventListeners() {
        this.copyBtn.addEventListener('click', () => this.copyCode());
        this.explainAllBtn.addEventListener('click', () => this.explainAll());
        this.startMonitorBtn.addEventListener('click', () => this.startSerialMonitor());
        this.backBtn.addEventListener('click', () => window.location.href = 'step4-coding.html');
        this.nextBtn.addEventListener('click', () => {
            showAlert('success', '모든 단계를 완료했습니다!', 'alert-message');
        });
        document.getElementById('logout-btn').addEventListener('click', () => Auth.logout());

        // ⭐ setupSettingsModal을 여기서 호출
        this.setupSettingsModal();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new AITutorPage();
});