// client/js/components/modal.js
import { $ } from '../utils/dom.js';

export function openModal({ title, bodyHtml, actions = [] }) {
    const root = $('#modal-root');
    root.innerHTML = `
    <div class="modal-backdrop"></div>
    <div class="modal">
      <div class="modal-header">
        <h3>${title}</h3>
        <button id="modal-close">×</button>
      </div>
      <div class="modal-body">${bodyHtml}</div>
      <div class="modal-footer">
        ${actions.map((a, i) => `<button data-i="${i}" class="${a.variant || ''}">${a.label}</button>`).join('')}
      </div>
    </div>
  `;
    root.style.display = 'block';
    $('#modal-close').onclick = closeModal;
    root.querySelector('.modal-backdrop').onclick = closeModal;
    root.querySelectorAll('.modal-footer button').forEach((btn) => {
        btn.onclick = () => {
            const i = btn.dataset.i;
            const act = actions[i];
            if (act?.onClick === 'close') closeModal();
            else if (typeof act?.onClick === 'function') act.onClick(closeModal);
        };
    });
}

export function closeModal() {
    const root = document.getElementById('modal-root');
    if (!root) return;
    root.innerHTML = '';
    root.style.display = 'none';
}