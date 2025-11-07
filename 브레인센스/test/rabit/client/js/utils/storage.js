// storage.js - localStorage 관리 유틸리티
export class Storage {
    static setItem(key, value) {
        try {
            const serialized = JSON.stringify(value);
            localStorage.setItem(`labit_${key}`, serialized);
        } catch (error) {
            console.error('Storage setItem error:', error);
        }
    }

    static getItem(key) {
        try {
            const serialized = localStorage.getItem(`labit_${key}`);
            return serialized ? JSON.parse(serialized) : null;
        } catch (error) {
            console.error('Storage getItem error:', error);
            return null;
        }
    }

    static removeItem(key) {
        localStorage.removeItem(`labit_${key}`);
    }

    static clear() {
        const keys = Object.keys(localStorage);
        keys.forEach(key => {
            if (key.startsWith('labit_')) {
                localStorage.removeItem(key);
            }
        });
    }
}