// Step7: 학습 결과 표시 + 실시간 테스트
const modelId = sessionStorage.getItem('modelId');
if (!modelId) location.replace('/step6.html');

const $ = (id) => document.getElementById(id);

async function loadModel() {
    try {
        // 서버에서 모델 상세 가져오기
        // 기대 스키마: { modelId, metrics:{trainAcc,testAcc,cm}, rules:[…] }
        const r = await fetch(`/api/ai/models/${modelId}`);
        if (!r.ok) throw new Error('모델 조회 실패');
        const m = await r.json();

        // KPI
        $('acc-train').textContent = `Train 정확도: ${(m.metrics?.trainAcc ?? 0 * 100).toFixed?.(0) || Math.round((m.metrics?.trainAcc||0)*100) }%`;
        $('acc-test').textContent  = `Test 정확도:  ${(m.metrics?.testAcc ?? 0 * 100).toFixed?.(0) || Math.round((m.metrics?.testAcc||0)*100) }%`;
        $('model-id').textContent  = `Model ID: ${m.modelId}`;

        // 규칙
        $('rules').textContent = m.rules
            ? JSON.stringify(m.rules, null, 2)
            : (m.metrics?.explain || '규칙 정보가 없습니다.');

        // 혼동행렬
        renderCM(m.metrics?.cm);
    } catch (e) {
        alert('모델 불러오기 오류: ' + e.message);
    }
}

function renderCM(cm) {
    if (!Array.isArray(cm)) { $('cm-wrap').innerHTML = '<div class="muted">혼동 행렬 데이터 없음</div>'; return; }
    const n = cm.length;
    let html = '<table class="cm"><thead><tr><th></th>';
    for (let j=0;j<n;j++) html += `<th>예측${j+1}</th>`;
    html += '</tr></thead><tbody>';
    for (let i=0;i<n;i++){
        html += `<tr><th>실제${i+1}</th>`;
        for (let j=0;j<n;j++) html += `<td>${cm[i][j]}</td>`;
        html += '</tr>';
    }
    html += '</tbody></table>';
    $('cm-wrap').innerHTML = html;
}

$('btn-predict').addEventListener('click', async () => {
    const v = $('val-input').value.trim();
    if (!v) return alert('값을 입력하세요.');
    try {
        // 기대 스키마: { label, confidence }
        const r = await fetch(`/api/ai/models/${modelId}/predict`, {
            method: 'POST',
            headers: { 'Content-Type':'application/json' },
            body: JSON.stringify({ value: Number(v) })
        });
        if (!r.ok) throw new Error('예측 실패');
        const out = await r.json();
        $('pred-out').textContent = `AI 예측: ${out.label} (신뢰도 ${Math.round((out.confidence||0)*100)}%)`;
    } catch (e) {
        $('pred-out').textContent = '오류: ' + e.message;
    }
});

$('btn-demo-stream').addEventListener('click', async () => {
    const fake = [720, 300, 520, 680, 250, 410, 790, 355, 615, 495];
    $('stream-log').textContent = '';
    for (const v of fake) {
        const r = await fetch(`/api/ai/models/${modelId}/predict`, {
            method:'POST', headers:{'Content-Type':'application/json'},
            body: JSON.stringify({ value: v })
        });
        const out = await r.json().catch(()=>({label:'-',confidence:0}));
        $('stream-log').textContent += `value=${v} → ${out.label} (${Math.round((out.confidence||0)*100)}%)\n`;
        await new Promise(res=>setTimeout(res, 250));
    }
});

$('btn-back').addEventListener('click', () => location.href = '../pages/step7-result.html');

loadModel();