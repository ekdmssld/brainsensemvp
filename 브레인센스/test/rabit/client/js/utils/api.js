// api.js - API 호출 유틸리티
import { API_BASE_URL } from '../config.js';

export class API {
    static async request(endpoint, options = {}) {
        const token = localStorage.getItem('labit_token');

        const config = {
            method: options.method || 'GET',
            headers: {
                'Content-Type': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` }),
                ...options.headers
            },
            ...(options.body && { body: JSON.stringify(options.body) })
        };

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || `HTTP Error: ${response.status}`);
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    static async get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    static async post(endpoint, body) {
        return this.request(endpoint, { method: 'POST', body });
    }

    static async put(endpoint, body) {
        return this.request(endpoint, { method: 'PUT', body });
    }

    static async delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
}