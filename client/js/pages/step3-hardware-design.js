// step3-hardware-design.js - 회로 설계 페이지 로직
import { API } from '../utils/api.js';
import { Auth } from '../utils/auth.js';
import { Storage } from '../utils/storage.js';
import { showAlert } from '../utils/dom.js';
import {SerialPortManager} from '../utils/serial.js';

class HardwareDesignPage {
    constructor() {
        this.selectedKit = Storage.getItem('selectedKit');
        this.projectId = Storage.getItem('projectId');
        this.selectedComponents = [];
        this.components = [];

        this.componentsGrid = document.getElementById('components-grid');
        this.selectedCountEl = document.getElementById('selected-count');
        this.backBtn = document.getElementById('back-btn');
        this.nextBtn = document.getElementById('next-btn');
        this.logoutBtn = document.getElementById('logout-btn');

        // Modal elements
        this.modal = document.getElementById('connection-modal');
        this.closeModalBtn = document.getElementById('close-modal');
        this.modalCancelBtn = document.getElementById('modal-cancel-btn');
        this.modalSelectBtn = document.getElementById('modal-select-btn');

        this.serialManager = new SerialPortManager();
        this.selectedPort = null;

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
        this.loadComponents();
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

    loadComponents() {
        // 키트의 센서 및 구성품 정보
        this.components = [
            {
                id: 'sensor-1',
                name: this.selectedKit.sensors?.[0]?.name || '압력 센서',
                type: this.selectedKit.sensors?.[0]?.type || 'FSR402',
                category: 'sensor',
                description: this.selectedKit.sensors?.[0]?.description || '압력을 감지하는 센서입니다',
                pin: this.selectedKit.sensors?.[0]?.pin || 'A0',
                icon: '📊',
                connections: [
                    { from: 'VCC', to: '5V', color: 'red' },
                    { from: 'GND', to: 'GND', color: 'black' },
                    { from: 'OUT', to: 'A0', color: 'yellow' }
                ],
                steps: [
                    '센서의 VCC 핀을 아두이노의 5V에 연결합니다',
                    '센서의 GND 핀을 아두이노의 GND에 연결합니다',
                    '센서의 OUT 핀을 아두이노의 A0 핀에 연결합니다',
                    '10kΩ 풀다운 저항을 OUT과 GND 사이에 연결합니다'
                ],
                notes: [
                    '센서에 과도한 압력을 가하지 마세요',
                    '극성을 확인하고 연결하세요'
                ]
            },
            {
                id: 'led-1',
                name: 'LED',
                type: '5mm LED',
                category: 'output',
                description: '상태를 시각적으로 표시하는 발광 다이오드입니다',
                pin: 'D7',
                icon: '💡',
                connections: [
                    { from: 'Anode(+)', to: 'D7', color: 'red' },
                    { from: 'Cathode(-)', to: 'GND', color: 'black' }
                ],
                steps: [
                    'LED의 긴 다리(양극)를 220Ω 저항을 통해 D7 핀에 연결합니다',
                    'LED의 짧은 다리(음극)를 GND에 직접 연결합니다',
                    '저항 없이 직접 연결하면 LED가 손상될 수 있습니다'
                ],
                notes: [
                    '극성을 반드시 확인하세요 (긴 다리가 +)',
                    '220Ω 저항을 반드시 사용하세요'
                ]
            },
            {
                id: 'buzzer-1',
                name: '부저',
                type: 'Piezo Buzzer',
                category: 'output',
                description: '소리로 알림을 제공하는 부품입니다',
                pin: 'D8',
                icon: '🔊',
                connections: [
                    { from: 'Positive(+)', to: 'D8', color: 'red' },
                    { from: 'Negative(-)', to: 'GND', color: 'black' }
                ],
                steps: [
                    '부저의 (+) 핀을 D8 핀에 연결합니다',
                    '부저의 (-) 핀을 GND에 연결합니다',
                    '일부 부저는 극성이 없을 수 있습니다'
                ],
                notes: [
                    '소리가 크므로 테스트 시 주의하세요'
                ]
            },
            {
                id: 'bluetooth-1',
                name: '블루투스 모듈',
                type: 'HC-06',
                category: 'communication',
                description: '무선 데이터 통신을 위한 블루투스 모듈입니다',
                pin: 'D2, D3',
                icon: '📡',
                connections: [
                    { from: 'VCC', to: '5V', color: 'red' },
                    { from: 'GND', to: 'GND', color: 'black' },
                    { from: 'TXD', to: 'D2 (RX)', color: 'blue' },
                    { from: 'RXD', to: 'D3 (TX)', color: 'green' }
                ],
                steps: [
                    '모듈의 VCC를 5V에 연결합니다',
                    '모듈의 GND를 GND에 연결합니다',
                    '모듈의 TXD를 아두이노의 D2(RX)에 연결합니다',
                    '모듈의 RXD를 아두이노의 D3(TX)에 연결합니다',
                    'SoftwareSerial 라이브러리를 사용하여 통신합니다'
                ],
                notes: [
                    'TX와 RX를 바꿔 연결하지 않도록 주의하세요',
                    '일부 모듈은 3.3V 전원을 사용합니다'
                ]
            }
        ];

        // 키트에 포함된 추가 센서들
        if (this.selectedKit.sensors && this.selectedKit.sensors.length > 1) {
            this.selectedKit.sensors.slice(1).forEach((sensor, index) => {
                this.components.push({
                    id: `sensor-${index + 2}`,
                    name: sensor.name,
                    type: sensor.type,
                    category: 'sensor',
                    description: sensor.description,
                    pin: sensor.pin,
                    icon: '📊',
                    connections: [
                        { from: 'VCC', to: '5V', color: 'red' },
                        { from: 'GND', to: 'GND', color: 'black' },
                        { from: 'OUT', to: sensor.pin, color: 'yellow' }
                    ],
                    steps: [
                        `센서의 VCC 핀을 아두이노의 5V에 연결합니다`,
                        `센서의 GND 핀을 아두이노의 GND에 연결합니다`,
                        `센서의 OUT 핀을 아두이노의 ${sensor.pin} 핀에 연결합니다`
                    ],
                    notes: []
                });
            });
        }

        this.renderComponents();
    }

    renderComponents() {
        this.componentsGrid.innerHTML = '';

        this.components.forEach(component => {
            const card = this.createComponentCard(component);
            this.componentsGrid.appendChild(card);
        });
    }

    createComponentCard(component) {
        const card = document.createElement('div');
        card.className = 'component-card bg-white rounded-xl shadow-md p-6 relative border-2 border-gray-200';
        card.dataset.componentId = component.id;

        const categoryColors = {
            sensor: 'bg-blue-100 text-blue-800',
            output: 'bg-green-100 text-green-800',
            communication: 'bg-purple-100 text-purple-800'
        };

        const categoryNames = {
            sensor: '센서',
            output: '출력',
            communication: '통신'
        };

        card.innerHTML = `
            <div class="text-center mb-4">
                <div class="text-5xl mb-3">${component.icon}</div>
                <h3 class="text-lg font-bold text-gray-800">${component.name}</h3>
                <p class="text-sm text-gray-500">${component.type}</p>
                <span class="inline-block mt-2 px-3 py-1 ${categoryColors[component.category]} rounded-full text-xs font-semibold">
                    ${categoryNames[component.category]}
                </span>
            </div>

            <div class="mb-4">
                <p class="text-sm text-gray-600 line-clamp-2">${component.description}</p>
            </div>

            <div class="bg-gray-50 rounded-lg p-3 mb-4">
                <p class="text-xs text-gray-600 mb-1">연결 핀:</p>
                <p class="font-mono text-sm font-bold text-indigo-600">${component.pin}</p>
            </div>

            <div class="space-y-2">
                <button 
                    class="view-connection-btn w-full px-4 py-2 bg-indigo-100 text-indigo-700 rounded-lg text-sm font-semibold hover:bg-indigo-200 transition"
                    data-component-id="${component.id}"
                >
                    🔌 연결 방법 보기
                </button>
                <button 
                    class="toggle-select-btn w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm font-semibold hover:bg-gray-200 transition"
                    data-component-id="${component.id}"
                >
                    선택하기
                </button>
            </div>
        `;

        // 이벤트 리스너
        const viewBtn = card.querySelector('.view-connection-btn');
        viewBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            this.showConnectionModal(component);
        });

        const toggleBtn = card.querySelector('.toggle-select-btn');
        toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            this.toggleComponent(component.id, card);
        });

        return card;
    }

    toggleComponent(componentId, card) {
        const index = this.selectedComponents.findIndex(c => c.id === componentId);
        const toggleBtn = card.querySelector('.toggle-select-btn');

        if (index >= 0) {
            // 선택 해제
            this.selectedComponents.splice(index, 1);
            card.classList.remove('selected');
            toggleBtn.textContent = '선택하기';
            toggleBtn.classList.remove('bg-indigo-600', 'text-white');
            toggleBtn.classList.add('bg-gray-100', 'text-gray-700');
        } else {
            // 선택
            const component = this.components.find(c => c.id === componentId);
            this.selectedComponents.push(component);
            card.classList.add('selected');
            toggleBtn.textContent = '✓ 선택됨';
            toggleBtn.classList.remove('bg-gray-100', 'text-gray-700');
            toggleBtn.classList.add('bg-indigo-600', 'text-white');
        }

        this.updateSelectedCount();
    }

    updateSelectedCount() {
        this.selectedCountEl.textContent = this.selectedComponents.length;

        // 최소 1개 센서 선택 확인
        const hasSensor = this.selectedComponents.some(c => c.category === 'sensor');
        this.nextBtn.disabled = !hasSensor;

        if (hasSensor) {
            this.nextBtn.classList.add('animate-pulse');
        } else {
            this.nextBtn.classList.remove('animate-pulse');
        }
    }

    showConnectionModal(component) {
        // 모달 내용 채우기
        document.getElementById('modal-title').textContent = component.name;
        document.getElementById('modal-type').textContent = component.type;
        document.getElementById('modal-description').textContent = component.description;

        // 핀 연결 정보
        const pinsHtml = component.connections.map(conn => `
            <div class="flex items-center space-x-2 mb-2">
                <span class="w-3 h-3 rounded-full" style="background-color: ${conn.color}"></span>
                <span class="font-mono text-sm"><strong>${conn.from}</strong> → <strong>${conn.to}</strong></span>
            </div>
        `).join('');
        document.getElementById('modal-pins').innerHTML = pinsHtml;

        // 다이어그램 (SVG 또는 텍스트)
        const diagramHtml = this.generateConnectionDiagram(component);
        document.getElementById('modal-diagram').innerHTML = diagramHtml;

        // 연결 순서
        const stepsHtml = component.steps.map(step => `<li>${step}</li>`).join('');
        document.getElementById('modal-steps').innerHTML = stepsHtml;

        // 주의사항
        if (component.notes && component.notes.length > 0) {
            const notesHtml = component.notes.map(note => `<li>${note}</li>`).join('');
            document.getElementById('modal-notes').innerHTML = notesHtml;
            document.getElementById('modal-notes-container').classList.remove('hidden');
        } else {
            document.getElementById('modal-notes-container').classList.add('hidden');
        }

        // 선택 버튼
        this.modalSelectBtn.onclick = () => {
            const card = document.querySelector(`[data-component-id="${component.id}"]`);
            if (card && !this.selectedComponents.find(c => c.id === component.id)) {
                this.toggleComponent(component.id, card);
            }
            this.closeModal();
        };

        this.modal.classList.remove('hidden');
    }

    generateConnectionDiagram(component) {
        // 간단한 텍스트 기반 다이어그램
        return `
            <div class="font-mono text-sm space-y-2">
                <div class="text-center font-bold text-gray-700 mb-4">연결 다이어그램</div>
                <div class="border-2 border-gray-300 rounded-lg p-4 bg-white">
                    <div class="grid grid-cols-2 gap-4">
                        <div class="text-center">
                            <div class="font-bold text-gray-700 mb-2">${component.name}</div>
                            ${component.connections.map(conn => `
                                <div class="py-1">
                                    <span class="inline-block w-20 px-2 py-1 bg-gray-100 rounded">${conn.from}</span>
                                </div>
                            `).join('')}
                        </div>
                        <div class="text-center">
                            <div class="font-bold text-gray-700 mb-2">Arduino</div>
                            ${component.connections.map(conn => `
                                <div class="py-1">
                                    <span class="inline-block w-20 px-2 py-1 bg-indigo-100 rounded">${conn.to}</span>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>
                <div class="text-xs text-gray-500 text-center mt-4">
                    실제 연결 시 와이어 색상을 참고하세요
                </div>
            </div>
        `;
    }

    closeModal() {
        this.modal.classList.add('hidden');
    }

    setupEventListeners() {
        this.closeModalBtn.addEventListener('click', () => this.closeModal());
        this.modalCancelBtn.addEventListener('click', () => this.closeModal());
        this.backBtn.addEventListener('click', () => this.goBack());
        this.nextBtn.addEventListener('click', () => this.goNext());
        this.logoutBtn.addEventListener('click', () => Auth.logout());
        this.setupSettingsModal();
    }

    goBack() {
        if (confirm('이전 단계로 돌아가시겠습니까?')) {
            window.location.href = 'step2-brainstorming.html';
        }
    }

    async goNext() {
        if (this.selectedComponents.length === 0) {
            showAlert('warning', '최소 1개 이상의 구성품을 선택해주세요', 'alert-message');
            return;
        }

        const hasSensor = this.selectedComponents.some(c => c.category === 'sensor');
        if (!hasSensor) {
            showAlert('warning', '최소 1개 이상의 센서를 선택해주세요', 'alert-message');
            return;
        }

        // 선택한 구성품 저장
        Storage.setItem('selectedComponents', this.selectedComponents);

        // 서버에 저장
        try {
            if (this.projectId) {
                await API.put(`/projects/${this.projectId}`, {
                    hardwareDesign: {
                        components: this.selectedComponents,
                        selectedAt: new Date().toISOString()
                    },
                    status: 'hardware'
                });
            }

            showAlert('success', '하드웨어 설계가 저장되었습니다!', 'alert-message');

            setTimeout(() => {
                window.location.href = 'step4-coding.html';
            }, 1000);

        } catch (error) {
            console.error('Save hardware design error:', error);
            // 저장 실패해도 다음 단계로 진행 (로컬에 저장됨)
            window.location.href = 'step4-coding.html';
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new HardwareDesignPage();
});