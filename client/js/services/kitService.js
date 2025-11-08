// client/js/services/kitService.js
import { API_BASE_URL } from '../config.js';

export const kitService = {
    async getComponents(kitId) {
        const res = await fetch(`${API_BASE_URL}/kits/${kitId}/components`, { credentials: 'include' });
        if (!res.ok) throw new Error('키트 구성 요소를 불러오지 못했습니다.');
        return res.json(); // { kitId, components: [...] }
    },
    async getComponentDetail(kitId, componentId) {
        const res = await fetch(`${API_BASE_URL}/kits/${kitId}/components/${componentId}`, { credentials: 'include' });
        if (!res.ok) throw new Error('구성 요소 상세를 불러오지 못했습니다.');
        return res.json();
    },
};