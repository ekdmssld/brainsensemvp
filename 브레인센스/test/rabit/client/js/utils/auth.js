// auth.js - 인증 관리 유틸리티
export class Auth {
    static TOKEN_KEY = 'labit_token';
    static USER_KEY = 'labit_user';

    // 토큰 저장
    static setToken(token) {
        localStorage.setItem(this.TOKEN_KEY, token);
    }

    // 토큰 가져오기
    static getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    // 토큰 삭제
    static removeToken() {
        localStorage.removeItem(this.TOKEN_KEY);
    }

    // 사용자 정보 저장
    static setUser(user) {
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }

    // 사용자 정보 가져오기
    static getUser() {
        const user = localStorage.getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    }

    // 사용자 정보 삭제
    static removeUser() {
        localStorage.removeItem(this.USER_KEY);
    }

    // 로그인 상태 확인
    static isAuthenticated() {
        const token = this.getToken();
        if (!token) return false;

        // JWT 토큰 만료 확인 (선택사항)
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const isExpired = payload.exp * 1000 < Date.now();

            if (isExpired) {
                this.logout();
                return false;
            }

            return true;
        } catch (error) {
            return false;
        }
    }

    // 로그아웃
    static logout() {
        this.removeToken();
        this.removeUser();
        window.location.href = 'login.html';
    }
}