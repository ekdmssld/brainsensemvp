// Step6: 학습 + 다음단계 이동
const btnTrain = document.getElementById('btn-train');
const btnNext  = document.getElementById('btn-next');
const btnCSV   = document.getElementById('btn-csv');
const btnReset = document.getElementById('btn-reset');

let latestModel = null;

// (선택) 페이지 진입 시 이전 진행상태 복구
const cachedModelId = sessionStorage.getItem('modelId');
if (cachedModelId) btnNext.disabled = false;

btnTrain.addEventListener('click', async () => {
    try {
        btnTrain.disabled = true;
        btnTrain.textContent = '학습 중…';

        // ▼ 서버 API 이름만 네 프로젝트에 맞게 바꾸면 됨
        const res = await fetch('/api/ai/train', { method: 'POST' });
        if (!res.ok) throw new Error('학습 API 실패');
        const data = await res.json();

        // 기대 스키마 예시: { modelId, metrics: {trainAcc, testAcc, cm}, rules }
        latestModel = data;
        sessionStorage.setItem('modelId', data.modelId);
        sessionStorage.setItem('metrics', JSON.stringify(data.metrics));

        btnNext.disabled = false;
        btnTrain.textContent = '✅ 학습 완료';
    } catch (e) {
        alert('학습 중 오류: ' + e.message);
    } finally {
        setTimeout(() => (btnTrain.disabled = false), 400);
    }
});

btnCSV.addEventListener('click', async () => {
    try {
        const r = await fetch('/api/dataset/csv');
        if (!r.ok) throw new Error('CSV 생성 실패');
        const blob = await r.blob();
        const url = URL.createObjectURL(blob);
        const a = Object.assign(document.createElement('a'), { href: url, download: 'dataset.csv' });
        document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
    } catch (e) { alert(e.message); }
});

btnReset.addEventListener('click', async () => {
    if (!confirm('정말 데이터/학습 상태를 초기화할까요?')) return;
    await fetch('/api/dataset/reset', { method: 'POST' }).catch(()=>{});
    sessionStorage.removeItem('modelId');
    sessionStorage.removeItem('metrics');
    btnNext.disabled = true;
    alert('데이터 초기화 완료');
});

btnNext.addEventListener('click', () => {
    const modelId = sessionStorage.getItem('modelId');
    if (!modelId) return alert('먼저 학습을 완료하세요.');
    location.href = './step7-result.html';
});

const $ = (s) => document.querySelector(s);
const $$ = (s) => Array.from(document.querySelectorAll(s));

const state = {
    collecting: false,
    currentLabel: "앉아있음",
    data: [], // { value:number, label:string, ts:Date }
    model: null, // { rules:[], reasoning:string }
};

const labels = ["앉아있음", "서있음", "걷는중"];

let port = null;
let reader = null;

function logLine(t) {
    const el = $("#serial-log");
    const line = document.createElement("div");
    line.textContent = t;
    el.appendChild(line);
    el.scrollTop = el.scrollHeight;
}

function setTutor(text) {
    const b = $("#tutor");
    $("#tutor-text").textContent = text;
    b.style.display = "block";
}

function hideTutor() { $("#tutor").style.display = "none"; }

function shuffle(arr) {
    const a = arr.slice();
    for (let i=a.length-1;i>0;i--){
        const j = Math.floor(Math.random()*(i+1));
        [a[i],a[j]]=[a[j],a[i]];
    }
    return a;
}

function calcStats(){
    const count = (lab) => state.data.filter(d=>d.label===lab).length;
    const avg = (lab) => {
        const arr = state.data.filter(d=>d.label===lab).map(d=>d.value);
        if(!arr.length) return "-";
        const m = Math.round(arr.reduce((a,b)=>a+b,0)/arr.length);
        const min = Math.min(...arr), max = Math.max(...arr);
        return `${m} (범위 ${min}-${max})`;
    };
    $("#total-count").textContent = state.data.length;
    $("#collected-count").textContent = state.data.length;
    $("#count-sit").textContent = count("앉아있음");
    $("#count-stand").textContent = count("서있음");
    $("#count-walk").textContent = count("걷는중");
    $("#avg-triplet").textContent = [avg("앉아있음"), avg("서있음"), avg("걷는중")].join(" / ");
    $("#ds-count").textContent = `총 ${state.data.length}개`;
}

function updateSplitUI(){
    const split = Number($("#split").value);
    $("#split-bar").style.width = split+"%";
    const n = state.data.length;
    const trainN = Math.floor(n*split/100);
    const testN = n - trainN;
    $("#train-count").textContent = trainN;
    $("#test-count").textContent = testN;
}

async function connectSerial(){
    try{
        port = await navigator.serial.requestPort();
        await port.open({ baudRate: 115200 });
        const decoder = new TextDecoderStream();
        const inputDone = port.readable.pipeTo(decoder.writable);
        reader = decoder.readable.getReader();

        logLine("[연결 완료]");
        setTutor("연결되었습니다. 라벨을 선택하고 '🔴 수집 시작'을 눌러 데이터를 만들어봅시다!");

        readLoop();
    }catch(e){
        console.error(e);
        logLine("[연결 실패] " + e.message);
    }
}

async function readLoop(){
    let buf = "";
    while (port && reader) {
        const {value, done} = await reader.read();
        if (done) break;
        if (!value) continue;
        buf += value;
        const lines = buf.split(/\r?\n/);
        buf = lines.pop() || "";
        for(const line of lines){
            if(!line.trim()) continue;
            logLine(line);
            if(state.collecting){
                const m = line.match(/(\d+)/);
                if(m){
                    const val = Number(m[1]);
                    state.data.push({ value: val, label: state.currentLabel, ts: new Date() });
                    calcStats();
                }
            }
        }
    }
}

function copyLog(){
    const text = $("#serial-log").innerText;
    navigator.clipboard.writeText(text);
}

function setLabel(lab){
    state.currentLabel = lab;
    $("#state-select").value = lab;
    $$(".label-select button").forEach(b=>{
        b.classList.toggle("active", b.dataset.label===lab);
    });
}

function toCSV(){
    const rows = ["value,label,timestamp"];
    for(const d of state.data){
        rows.push([d.value, d.label, d.ts.toISOString().replace("T"," ").split(".")[0]].join(","));
    }
    return rows.join("\n");
}

function downloadCSV(){
    const csv = toCSV();
    const blob = new Blob([csv], {type:"text/csv"});
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url; a.download = "dataset.csv";
    a.click();
    URL.revokeObjectURL(url);
}

function resetAll(){
    state.data = [];
    state.model = null;
    $("#model-status").textContent = "모델 없음";
    hideTutor();
    calcStats();
    updateSplitUI();
    $("#result").style.display="none";
    $("#cmat").innerHTML="";
    $("#ai-reason").textContent="";
}

function splitData(){
    const split = Number($("#split").value);
    const s = shuffle(state.data);
    const idx = Math.floor(s.length*split/100);
    return { train: s.slice(0,idx), test: s.slice(idx) };
}

function applyRules(rules, v){
    // 아주 단순한 규칙 실행기: 상단부터 최초로 만족하는 label 사용
    for(const r of rules){
        // r.condition: e.g. "value >= 600"
        const expr = r.condition.replace(/value/g, String(v));
        try{
            // eslint-disable-next-line no-eval
            if(eval(expr)) return r.label;
        }catch(_){}
    }
    // 아무 규칙도 안걸리면 마지막 default가 있다면 사용
    return rules.find(r => /else/i.test(r.condition))?.label ?? "Unknown";
}

function buildConfusionMatrix(labelsArr, yTrue, yPred){
    const idx = new Map(labelsArr.map((l,i)=>[l,i]));
    const mat = Array.from({length:labelsArr.length},()=>Array(labelsArr.length).fill(0));
    for(let i=0;i<yTrue.length;i++){
        const r = idx.get(yTrue[i]), c = idx.get(yPred[i]);
        if(r!=null && c!=null) mat[r][c]++;
    }
    return mat;
}

function renderCM(labelsArr, mat){
    const el = $("#cmat");
    let html = "<tr><th></th>";
    for(const l of labelsArr) html += `<th>예측:${l}</th>`;
    html += "</tr>";
    for(let r=0;r<labelsArr.length;r++){
        html += `<tr><th>실제:${labelsArr[r]}</th>`;
        for(let c=0;c<labelsArr.length;c++){
            html += `<td>${mat[r][c]}</td>`;
        }
        html += "</tr>";
    }
    el.innerHTML = html;
}

async function trainModel(){
    if(state.data.length < 9){
        setTutor("각 라벨마다 최소 3개 이상 수집해 주세요. (예: 앉음/서있음/걷기 각각 ≥3)");
        return;
    }
    const {train, test} = splitData();

    const toVals = (lab, arr) => arr.filter(d=>d.label===lab).map(d=>d.value);

    // 1) Ollama로 규칙 생성 요청
    const payload = {
        sit: toVals("앉아있음", train),
        stand: toVals("서있음", train),
        walk: toVals("걷는중", train)
    };

    setTutor("AI가 데이터를 분석 중입니다… (규칙 생성)");
    $("#model-status").textContent = "학습 중…";

    const res = await fetch("/api/ai/train-rules", {
        method:"POST",
        headers:{"Content-Type":"application/json"},
        body: JSON.stringify(payload)
    });
    const data = await res.json();
    if(!data?.success){ setTutor(data?.message || "학습 실패"); return; }

    // 2) 모델 저장
    state.model = data.model; // { rules, reasoning }
    $("#model-status").textContent = "학습 완료";

    // 3) 정확도/혼동행렬 (Train/Test 각각)
    const evalSet = (arr)=>{
        const yTrue = arr.map(d=>d.label);
        const yPred = arr.map(d=>applyRules(state.model.rules, d.value));
        const correct = yTrue.filter((y,i)=>y===yPred[i]).length;
        const acc = Math.round((correct / (arr.length||1))*100);
        const mat = buildConfusionMatrix(labels, yTrue, yPred);
        return {acc, mat};
    };

    const tr = evalSet(train), te = evalSet(test);

    // 4) 화면 표시
    $("#result").style.display = "block";
    $("#acc-train").textContent = tr.acc + "%";
    $("#acc-test").textContent  = te.acc + "%";
    renderCM(labels, te.mat);
    $("#ai-reason").textContent = state.model.reasoning || "(이유 설명 없음)";

    setTutor("학습이 끝났습니다. 오른쪽 결과를 확인해 보세요!");
}

// 실시간 테스트(간단): 최근 센서 1개를 예측
function realtimeTest(){
    const last = state.data[state.data.length-1];
    if(!last || !state.model){ setTutor("모델 학습 후, 센서값이 들어왔을 때 예측해 볼 수 있어요."); return; }
    const pred = applyRules(state.model.rules, last.value);
    setTutor(`현재 센서값 ${last.value} → 예측: ${pred}`);
}

function bind(){
    // 시리얼
    $("#btn-connect").addEventListener("click", connectSerial);
    $("#btn-copy").addEventListener("click", copyLog);

    // 라벨 선택
    $$("#state-select").forEach(()=>{}); // no-op
    $("#state-select").addEventListener("change", (e)=> setLabel(e.target.value));
    $$(".label-select button").forEach(b=>{
        b.addEventListener("click", ()=> setLabel(b.dataset.label));
    });

    // 수집
    $("#btn-start").addEventListener("click", ()=>{
        state.collecting = true;
        $("#collecting-state").textContent = "데이터 수집 중…";
        setTutor(`'${state.currentLabel}' 데이터를 모으는 중입니다. 센서에 동작을 해보세요!`);
    });
    $("#btn-stop").addEventListener("click", ()=>{
        state.collecting = false;
        $("#collecting-state").textContent = "대기 중";
        setTutor("수집을 멈췄습니다. 라벨을 바꾸거나 학습을 진행해 보세요.");
    });

    // Train/Test
    $("#split").addEventListener("input", updateSplitUI);

    // 버튼들
    $("#btn-download").addEventListener("click", downloadCSV);
    $("#btn-reset").addEventListener("click", resetAll);
    $("#btn-train").addEventListener("click", trainModel);
    $("#btn-realtime").addEventListener("click", realtimeTest);
    $("#btn-save-model").addEventListener("click", ()=>{
        if(!state.model){ setTutor("먼저 학습을 완료해 주세요."); return; }
        const blob = new Blob([JSON.stringify(state.model,null,2)], {type:"application/json"});
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url; a.download = "model.json"; a.click();
        URL.revokeObjectURL(url);
    });

    // 초기 UI
    calcStats();
    updateSplitUI();
    setTutor("시리얼을 연결하고, 라벨을 선택한 뒤 ‘수집 시작’을 눌러 데이터를 만들어 봅시다!");
}

document.addEventListener("DOMContentLoaded", bind);