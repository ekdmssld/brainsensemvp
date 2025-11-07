// step1-kit-selection.js - 키트 선택 페이지 로직
import { API } from '../utils/api.js';
import { Auth } from '../utils/auth.js';
import { showAlert } from '../utils/dom.js';
import { Storage } from '../utils/storage.js';

class KitSelectionPage {
    constructor() {
        this.kits = [];
        this.categories = [];
        this.selectedKit = null;

        this.categorySelect = document.getElementById('category-select');
        this.kitsContainer = document.getElementById('kits-container');
        this.loadingSpinner = document.getElementById('loading-spinner');
        this.noKitsMessage = document.getElementById('no-kits-message');
        this.nextButton = document.getElementById('next-button-container');
        this.logoutBtn = document.getElementById('logout-btn');

        // Modal elements
        this.modal = document.getElementById('kit-modal');
        this.modalTitle = document.getElementById('modal-title');
        this.modalImage = document.getElementById('modal-image');
        this.modalDescription = document.getElementById('modal-description');
        this.modalComponents = document.getElementById('modal-components');
        this.modalObjectives = document.getElementById('modal-objectives');
        this.modalDifficulty = document.getElementById('modal-difficulty');
        this.modalSelectBtn = document.getElementById('modal-select-btn');
        this.closeModalBtn = document.getElementById('close-modal');
        this.modalCancelBtn = document.getElementById('modal-cancel-btn');

        this.init();
    }

    async init() {
        // 로그인 확인
        if (!Auth.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }

        // 사용자 정보 표시
        this.displayUserInfo();

        // 이벤트 리스너
        this.categorySelect.addEventListener('change', () => this.filterKitsByCategory());
        this.logoutBtn.addEventListener('click', () => this.handleLogout());
        this.closeModalBtn.addEventListener('click', () => this.closeModal());
        this.modalCancelBtn.addEventListener('click', () => this.closeModal());

        // 키트 목록 로드
        await this.loadKits();
    }

    displayUserInfo() {
        const user = Auth.getUser();
        if (user) {
            document.getElementById('user-name').textContent = user.name;
            document.getElementById('user-email').textContent = user.email;
        }
    }

    async loadKits() {
        try {
            this.showLoading(true);

            const response = await API.get('/kits');
            this.kits = response.kits;

            // 카테고리 추출
            this.categories = [...new Set(this.kits.map(kit => kit.category))];
            this.populateCategories();

            // 모든 키트 표시
            this.displayKits(this.kits);

            this.showLoading(false);
        } catch (error) {
            console.error('Load kits error:', error);
            showAlert('error', '키트 목록을 불러오는데 실패했습니다', 'alert-message');
            this.showLoading(false);
        }
    }

    populateCategories() {
        // 카테고리 옵션 추가
        this.categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category;
            option.textContent = category;
            this.categorySelect.appendChild(option);
        });
    }

    filterKitsByCategory() {
        const selectedCategory = this.categorySelect.value;

        if (!selectedCategory) {
            this.displayKits(this.kits);
        } else {
            const filteredKits = this.kits.filter(kit => kit.category === selectedCategory);
            this.displayKits(filteredKits);
        }
    }

    displayKits(kits) {
        this.kitsContainer.innerHTML = '';

        if (kits.length === 0) {
            this.noKitsMessage.classList.remove('hidden');
            return;
        }

        this.noKitsMessage.classList.add('hidden');

        kits.forEach(kit => {
            const kitCard = this.createKitCard(kit);
            this.kitsContainer.appendChild(kitCard);
        });
    }

    createKitCard(kit) {
        const card = document.createElement('div');
        card.className = 'bg-white rounded-xl shadow-md hover:shadow-xl transition-all duration-300 overflow-hidden cursor-pointer transform hover:-translate-y-1';

        card.innerHTML = `
            <div class="relative">
                <img src="${kit.image || '../assets/images/kits/default.jpg'}" alt="${kit.name}" class="w-full h-48 object-cover">
                <div class="absolute top-3 right-3 bg-white px-3 py-1 rounded-full text-xs font-semibold text-indigo-600">
                    ${kit.category}
                </div>
            </div>
            <div class="p-5">
                <h3 class="text-xl font-bold text-gray-800 mb-2">${kit.name}</h3>
                <p class="text-gray-600 text-sm mb-4 line-clamp-2">${kit.description}</p>
                
                <div class="flex items-center justify-between mb-4">
                    <div class="flex items-center space-x-1">
                        <span class="text-xs text-gray-500">난이도:</span>
                        ${this.renderDifficulty(kit.difficulty)}
                    </div>
                    <span class="text-xs text-gray-500">${kit.duration || '60'}분</span>
                </div>

                <div class="flex space-x-2">
                    <button 
                        class="flex-1 px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-semibold hover:bg-indigo-700 transition"
                        onclick="window.kitSelectionPage.selectKit('${kit._id}')"
                    >
                        선택하기
                    </button>
                    <button 
                        class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm font-semibold hover:bg-gray-200 transition"
                        onclick="window.kitSelectionPage.showKitDetail('${kit._id}')"
                    >
                        상세보기
                    </button>
                </div>
            </div>
        `;

        return card;
    }

    renderDifficulty(level) {
        const maxLevel = 3;
        const difficulty = level || 1;
        let stars = '';

        for (let i = 0; i < maxLevel; i++) {
            if (i < difficulty) {
                stars += '<span class="text-yellow-400">★</span>';
            } else {
                stars += '<span class="text-gray-300">★</span>';
            }
        }

        return stars;
    }

    showKitDetail(kitId) {
        const kit = this.kits.find(k => k._id === kitId);
        if (!kit) return;

        this.modalTitle.textContent = kit.name;
        this.modalImage.src = kit.image || '../assets/images/kits/default.jpg';
        this.modalDescription.textContent = kit.description;

        // 구성품 리스트
        this.modalComponents.innerHTML = '';
        kit.components?.forEach(component => {
            const li = document.createElement('li');
            li.textContent = component;
            this.modalComponents.appendChild(li);
        });

        // 학습 목표
        this.modalObjectives.innerHTML = '';
        kit.learningObjectives?.forEach(objective => {
            const li = document.createElement('li');
            li.textContent = objective;
            this.modalObjectives.appendChild(li);
        });

        // 난이도
        this.modalDifficulty.innerHTML = this.renderDifficulty(kit.difficulty);

        // 선택 버튼 이벤트
        this.modalSelectBtn.onclick = () => {
            this.selectKit(kitId);
            this.closeModal();
        };

        this.modal.classList.remove('hidden');
    }

    closeModal() {
        this.modal.classList.add('hidden');
    }

    selectKit(kitId) {
        const kit = this.kits.find(k => k._id === kitId);
        if (!kit) return;

        this.selectedKit = kit;

        // 선택된 키트를 로컬 스토리지에 저장
        Storage.setItem('selectedKit', kit);


        // 헤더에 키트명 표시
        document.getElementById('selected-kit-name').textContent = kit.name;

        // 다음 버튼 표시
        this.nextButton.classList.remove('hidden');

        // 성공 메시지
        showAlert('success', `"${kit.name}" 키트가 선택되었습니다!`, 'alert-message');

        // 다음 단계 버튼 이벤트
        document.getElementById('next-step-btn').onclick = () => {
            window.location.href = 'step2-brainstorming.html';
        };

        // 모든 카드의 선택 상태 초기화
        document.querySelectorAll('.kit-card-selected').forEach(card => {
            card.classList.remove('kit-card-selected');
        });

        // 스크롤 상단으로
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    handleLogout() {
        if (confirm('로그아웃 하시겠습니까?')) {
            Auth.logout();
        }
    }

    showLoading(isLoading) {
        if (isLoading) {
            this.loadingSpinner.classList.remove('hidden');
            this.kitsContainer.classList.add('opacity-50');
        } else {
            this.loadingSpinner.classList.add('hidden');
            this.kitsContainer.classList.remove('opacity-50');
        }
    }
}

// 전역 변수로 페이지 인스턴스 저장 (카드 버튼에서 접근하기 위해)
window.kitSelectionPage = null;

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    window.kitSelectionPage = new KitSelectionPage();
});