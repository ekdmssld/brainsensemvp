// dom.js - DOM 조작 헬퍼 함수
export function showAlert(type, message, elementId = 'alert-message') {
    const alertElement = document.getElementById(elementId);
    if (!alertElement) return;

    // 타입별 스타일
    const styles = {
        success: 'bg-green-100 border-green-400 text-green-700',
        error: 'bg-red-100 border-red-400 text-red-700',
        warning: 'bg-yellow-100 border-yellow-400 text-yellow-700',
        info: 'bg-blue-100 border-blue-400 text-blue-700'
    };

    const icons = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ'
    };

    // 메시지 표시
    alertElement.className = `border-l-4 p-4 rounded-lg ${styles[type] || styles.info}`;
    alertElement.innerHTML = `
        <div class="flex items-center">
            <span class="text-xl mr-3">${icons[type] || icons.info}</span>
            <span>${message}</span>
        </div>
    `;
    alertElement.classList.remove('hidden');

    // 5초 후 자동 숨김
    setTimeout(() => {
        alertElement.classList.add('hidden');
    }, 5000);
}

export function createElement(tag, attributes = {}, children = []) {
    const element = document.createElement(tag);

    Object.entries(attributes).forEach(([key, value]) => {
        if (key === 'className') {
            element.className = value;
        } else if (key.startsWith('on')) {
            element.addEventListener(key.substring(2).toLowerCase(), value);
        } else {
            element.setAttribute(key, value);
        }
    });

    children.forEach(child => {
        if (typeof child === 'string') {
            element.appendChild(document.createTextNode(child));
        } else {
            element.appendChild(child);
        }
    });

    return element;
}