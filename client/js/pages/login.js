// login.js - 로그인 페이지 로직
import { API } from '../utils/api.js';
import { Auth } from '../utils/auth.js';
import { showAlert } from '../utils/dom.js';

class LoginPage {
    constructor() {
        this.form = document.getElementById('login-form');
        this.emailInput = document.getElementById('email');
        this.passwordInput = document.getElementById('password');
        this.loginButton = document.getElementById('login-button');

        this.init();
    }

    init() {
        // 이미 로그인되어 있으면 키트 선택 페이지로 이동
        if (Auth.isAuthenticated()) {
            window.location.href = 'step1-kit-selection.html';
            return;
        }

        // 폼 제출 이벤트
        this.form.addEventListener('submit', (e) => this.handleLogin(e));

        // Enter 키 처리
        this.passwordInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.handleLogin(e);
            }
        });
    }

    async handleLogin(e) {
        e.preventDefault();

        const email = this.emailInput.value.trim();
        const password = this.passwordInput.value;

        // 기본 검증
        if (!this.validateForm(email, password)) {
            return;
        }

        // 로딩 상태
        this.setLoading(true);

        try {
            // API 호출
            const response = await API.post('/auth/login', { email, password });

            // 토큰 저장
            Auth.setToken(response.token);
            Auth.setUser(response.user);

            // 성공 메시지
            showAlert('success', '로그인 성공! 환영합니다.', 'alert-message');

            // 1초 후 키트 선택 페이지로 이동
            setTimeout(() => {
                window.location.href = 'step1-kit-selection.html';
            }, 1000);

        } catch (error) {
            console.error('Login error:', error);
            showAlert('error', error.message || '로그인에 실패했습니다. 다시 시도해주세요.', 'alert-message');
            this.setLoading(false);
        }
    }

    validateForm(email, password) {
        // 이메일 검증
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            showAlert('error', '올바른 이메일 형식을 입력해주세요.', 'alert-message');
            this.emailInput.focus();
            return false;
        }

        // 비밀번호 검증
        if (password.length < 6) {
            showAlert('error', '비밀번호는 최소 6자 이상이어야 합니다.', 'alert-message');
            this.passwordInput.focus();
            return false;
        }

        return true;
    }

    setLoading(isLoading) {
        this.loginButton.disabled = isLoading;
        this.loginButton.textContent = isLoading ? '로그인 중...' : '로그인';

        if (isLoading) {
            this.loginButton.classList.add('opacity-70', 'cursor-not-allowed');
        } else {
            this.loginButton.classList.remove('opacity-70', 'cursor-not-allowed');
        }
    }
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    new LoginPage();
});