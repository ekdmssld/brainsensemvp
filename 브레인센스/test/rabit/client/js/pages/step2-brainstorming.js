// step2-brainstorming.js - 브레인스토밍 페이지 로직
import { API } from '../utils/api.js';
import { Auth } from '../utils/auth.js';
import { Storage } from '../utils/storage.js';
import { showAlert } from '../utils/dom.js';

class BrainstormingPage {
    constructor() {
        this.selectedKit = Storage.getItem('selectedKit');
        this.notes = {
            A: [],
            B: [],
            C: []
        };
        this.noteIdCounter = 0;
        this.draggedElement = null;
        this.dragOffset = { x: 0, y: 0 };

        // DOM elements
        this.addNoteButtons = document.querySelectorAll('.add-note-btn');
        this.dropZones = document.querySelectorAll('.drop-zone');
        this.backBtn = document.getElementById('back-btn');
        this.nextBtn = document.getElementById('next-btn');
        this.logoutBtn = document.getElementById('logout-btn');
        this.totalNotesSpan = document.getElementById('total-notes');

        this.init();
    }

    init() {
        // 인증 확인
        if (!Auth.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }

        // 키트 선택 확인
        if (!this.selectedKit) {
            showAlert('error', '키트를 먼저 선택해주세요', 'alert-message');
            setTimeout(() => {
                window.location.href = 'step1-kit-selection.html';
            }, 2000);
            return;
        }

        // UI 초기화
        this.displayUserInfo();
        this.displayKitName();

        // 저장된 브레인스토밍 데이터 로드
        this.loadSavedNotes();

        // 이벤트 리스너
        this.addNoteButtons.forEach(btn => {
            btn.addEventListener('click', (e) => this.addNote(e.target.dataset.section));
        });

        this.backBtn.addEventListener('click', () => this.goBack());
        this.nextBtn.addEventListener('click', () => this.goNext());
        this.logoutBtn.addEventListener('click', () => Auth.logout());
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

    loadSavedNotes() {
        const savedNotes = Storage.getItem('brainstormingNotes');
        if (savedNotes) {
            this.notes = savedNotes;

            // 각 섹션의 노트 렌더링
            Object.keys(this.notes).forEach(section => {
                this.notes[section].forEach(note => {
                    this.renderNote(section, note);
                });
            });

            this.updateTotalNotes();
        }
    }

    addNote(section) {
        const note = {
            id: `note-${this.noteIdCounter++}-${Date.now()}`,
            content: '여기에 아이디어를 입력하세요...',
            x: Math.random() * 200 + 50, // 랜덤 위치
            y: Math.random() * 200 + 50,
            color: this.getNoteColor(section)
        };

        this.notes[section].push(note);
        this.renderNote(section, note);
        this.updateTotalNotes();
        this.saveNotes();

        // 새로 추가된 노트 자동 편집 모드
        setTimeout(() => {
            const noteElement = document.getElementById(note.id);
            if (noteElement) {
                this.editNote(noteElement);
            }
        }, 100);
    }

    getNoteColor(section) {
        const colors = {
            A: 'bg-yellow-200',
            B: 'bg-pink-200',
            C: 'bg-green-200'
        };
        return colors[section] || 'bg-blue-200';
    }

    renderNote(section, note) {
        const zone = document.getElementById(`zone-${section}`);
        const noteElement = document.createElement('div');

        noteElement.id = note.id;
        noteElement.className = `sticky-note ${note.color} p-4 rounded-lg w-48 min-h-32`;
        noteElement.style.left = `${note.x}px`;
        noteElement.style.top = `${note.y}px`;
        noteElement.draggable = true;

        noteElement.innerHTML = `
            <div class="flex justify-between items-start mb-2">
                <div class="text-xs text-gray-500 font-semibold">${section}</div>
                <button class="delete-note text-red-500 hover:text-red-700 text-sm font-bold" data-note-id="${note.id}" data-section="${section}">
                    ✕
                </button>
            </div>
            <div class="note-content text-sm text-gray-800 whitespace-pre-wrap break-words">
                ${note.content}
            </div>
        `;

        // 드래그 이벤트
        noteElement.addEventListener('dragstart', (e) => this.handleDragStart(e));
        noteElement.addEventListener('dragend', (e) => this.handleDragEnd(e));

        // 더블클릭으로 편집
        noteElement.addEventListener('dblclick', () => this.editNote(noteElement));

        // 삭제 버튼
        const deleteBtn = noteElement.querySelector('.delete-note');
        deleteBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            this.deleteNote(e.target.dataset.noteId, e.target.dataset.section);
        });

        zone.appendChild(noteElement);

        // 드롭존 이벤트 (처음 한 번만)
        if (!zone.hasAttribute('data-listeners-added')) {
            zone.addEventListener('dragover', (e) => this.handleDragOver(e));
            zone.addEventListener('drop', (e) => this.handleDrop(e));
            zone.addEventListener('dragenter', (e) => this.handleDragEnter(e));
            zone.addEventListener('dragleave', (e) => this.handleDragLeave(e));
            zone.setAttribute('data-listeners-added', 'true');
        }
    }

    handleDragStart(e) {
        this.draggedElement = e.target;
        this.draggedElement.classList.add('dragging');

        const rect = this.draggedElement.getBoundingClientRect();
        const parentRect = this.draggedElement.parentElement.getBoundingClientRect();

        this.dragOffset.x = e.clientX - rect.left;
        this.dragOffset.y = e.clientY - rect.top;

        e.dataTransfer.effectAllowed = 'move';
    }

    handleDragEnd(e) {
        if (this.draggedElement) {
            this.draggedElement.classList.remove('dragging');
            this.draggedElement = null;
        }

        // 모든 드롭존의 하이라이트 제거
        this.dropZones.forEach(zone => zone.classList.remove('drag-over'));
    }

    handleDragOver(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
    }

    handleDragEnter(e) {
        if (e.target.classList.contains('drop-zone')) {
            e.target.classList.add('drag-over');
        }
    }

    handleDragLeave(e) {
        if (e.target.classList.contains('drop-zone')) {
            e.target.classList.remove('drag-over');
        }
    }

    handleDrop(e) {
        e.preventDefault();

        if (!this.draggedElement) return;

        const dropZone = e.currentTarget;
        dropZone.classList.remove('drag-over');

        // 드롭존 내의 상대 위치 계산
        const rect = dropZone.getBoundingClientRect();
        const x = e.clientX - rect.left - this.dragOffset.x;
        const y = e.clientY - rect.top - this.dragOffset.y;

        // 위치 제한 (드롭존 내부에만)
        const maxX = rect.width - this.draggedElement.offsetWidth;
        const maxY = rect.height - this.draggedElement.offsetHeight;

        const finalX = Math.max(0, Math.min(x, maxX));
        const finalY = Math.max(0, Math.min(y, maxY));

        this.draggedElement.style.left = `${finalX}px`;
        this.draggedElement.style.top = `${finalY}px`;

        // 새로운 섹션으로 이동
        const newSection = dropZone.dataset.section;
        const oldSection = this.draggedElement.querySelector('.delete-note').dataset.section;

        if (newSection !== oldSection) {
            dropZone.appendChild(this.draggedElement);

            // 데이터 업데이트
            const noteId = this.draggedElement.id;
            const noteIndex = this.notes[oldSection].findIndex(n => n.id === noteId);

            if (noteIndex !== -1) {
                const note = this.notes[oldSection].splice(noteIndex, 1)[0];
                note.color = this.getNoteColor(newSection);
                this.notes[newSection].push(note);

                // 색상 업데이트
                this.draggedElement.className = `sticky-note ${note.color} p-4 rounded-lg w-48 min-h-32`;

                // 섹션 레이블 업데이트
                this.draggedElement.querySelector('.text-xs').textContent = newSection;
                this.draggedElement.querySelector('.delete-note').dataset.section = newSection;
            }
        }

        // 위치 저장
        this.updateNotePosition(this.draggedElement.id, finalX, finalY);
    }

    updateNotePosition(noteId, x, y) {
        Object.keys(this.notes).forEach(section => {
            const note = this.notes[section].find(n => n.id === noteId);
            if (note) {
                note.x = x;
                note.y = y;
            }
        });
        this.saveNotes();
    }

    editNote(noteElement) {
        const contentDiv = noteElement.querySelector('.note-content');
        const currentContent = contentDiv.textContent.trim();

        const textarea = document.createElement('textarea');
        textarea.className = 'w-full h-20 p-2 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none';
        textarea.value = currentContent;

        contentDiv.replaceWith(textarea);
        textarea.focus();
        textarea.select();

        const saveEdit = () => {
            const newContent = textarea.value.trim() || '빈 노트';
            const newContentDiv = document.createElement('div');
            newContentDiv.className = 'note-content text-sm text-gray-800 whitespace-pre-wrap break-words';
            newContentDiv.textContent = newContent;

            textarea.replaceWith(newContentDiv);

            // 데이터 업데이트
            const noteId = noteElement.id;
            Object.keys(this.notes).forEach(section => {
                const note = this.notes[section].find(n => n.id === noteId);
                if (note) {
                    note.content = newContent;
                }
            });
            this.saveNotes();
        };

        textarea.addEventListener('blur', saveEdit);
        textarea.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.ctrlKey) {
                saveEdit();
            }
            if (e.key === 'Escape') {
                textarea.blur();
            }
        });
    }

    deleteNote(noteId, section) {
        if (confirm('이 포스트잇을 삭제하시겠습니까?')) {
            const noteElement = document.getElementById(noteId);
            if (noteElement) {
                noteElement.remove();
            }

            this.notes[section] = this.notes[section].filter(n => n.id !== noteId);
            this.updateTotalNotes();
            this.saveNotes();
        }
    }

    updateTotalNotes() {
        const total = Object.values(this.notes).reduce((sum, notes) => sum + notes.length, 0);
        this.totalNotesSpan.textContent = total;
    }

    saveNotes() {
        Storage.setItem('brainstormingNotes', this.notes);
    }

    goBack() {
        if (confirm('이전 단계로 돌아가시겠습니까? (작성 내용은 저장됩니다)')) {
            window.location.href = 'step1-kit-selection.html';
        }
    }

    async goNext() {
        // 검증: 각 섹션에 최소 1개 이상의 노트가 있는지 확인
        const hasNotesInA = this.notes.A.length > 0;
        const hasNotesInB = this.notes.B.length > 0;
        const hasNotesInC = this.notes.C.length > 0;

        if (!hasNotesInA || !hasNotesInB || !hasNotesInC) {
            showAlert('warning', '각 질문에 최소 1개 이상의 아이디어를 작성해주세요', 'alert-message');
            return;
        }

        // 서버에 저장
        try {
            const projectData = {
                kitId: this.selectedKit._id,
                kitName: this.selectedKit.name,
                brainstorming: this.notes
            };

            // 프로젝트 ID가 있으면 업데이트, 없으면 생성
            const projectId = Storage.getItem('projectId');

            let response;
            if (projectId) {
                response = await API.put(`/projects/${projectId}`, projectData);
            } else {
                response = await API.post('/projects', projectData);
                Storage.setItem('projectId', response.project._id);
            }

            showAlert('success', '브레인스토밍이 저장되었습니다!', 'alert-message');

            setTimeout(() => {
                window.location.href = 'step3-hardware-design.html';
            }, 1000);

        } catch (error) {
            console.error('Save brainstorming error:', error);
            showAlert('error', '저장 중 오류가 발생했습니다', 'alert-message');
        }
    }
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    new BrainstormingPage();
});