/* balanced diet - script.js
   기능별로 번호 매겨서 정리함

   흐름:
   1. state 변수 하나에 데이터 다 모아둠
   2. localStorage에 저장/불러오기
   3. 화면 전환은 class 붙였다 뗐다 하는 걸로만 함
   4. AI 호출 함수는 callAIForGoal, callAIForMeal, callAIForMealPhoto 이렇게 3개
   ========================================================= */

//  1. state 저장/불러오기

const STORAGE_KEY = "balancedDietState";

// 오늘 날짜 구하기 (날짜 바뀌면 기록 초기화할 때 씀)
function getTodayString() {
    return new Date().toISOString().slice(0, 10);
}

// 앱에서 쓰는 데이터 다 여기 모아둠

let state = {
    profile: { height: "", weight: "", age: "", gender: "male", activity: "mid" },
    goalType: "",     // "bulk" | "diet" | "health" | "custom"
    customGoalText: "", // 기타 선택했을 때 직접 쓴 목표
    goalMethod: "",   // "ai" | "manual"
    goal: { calories: 0, carb: 0, protein: 0, fat: 0 },
    date: getTodayString(),
    meals: [],         // { name, calories, carb, protein, fat }
    history: {},      // 날짜별 하루 합계 - 주간 평균용
};

function saveState() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function loadState() {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return;
    const saved = JSON.parse(raw);

    // 날짜 바뀌면 식단 기록만 리셋 (목표는 그대로 둠)
    if (saved.date !== getTodayString()) {
        saved.date = getTodayString();
        saved.meals = [];
    }

    if (!saved.history) saved.history = {};

    state = saved;
}

//  2. 화면 전환, 뒤로가기

// 지나온 화면들 순서대로 쌓아두는 배열 (뒤로가기용)
let screenHistory = [];

// 지금 보이는 화면 id 가져오기
function getCurrentScreenId() {
    const current = document.querySelector(".screen.active");
    return current ? current.id : null;
}

// 화면만 바꿔주는 함수 (기록은 안 남김)
function switchScreen(id) {
    document.querySelectorAll(".screen").forEach(el => el.classList.remove("active"));
    document.getElementById(id).classList.add("active");
}

// 다음 화면으로 넘어갈 때 쓰는 함수 (지금 화면 기록해두고 이동)
function showScreen(id) {
    const current = getCurrentScreenId();
    if (current && current !== id) {
        screenHistory.push(current);
    }
    switchScreen(id);
}

// 뒤로가기 버튼 누르면 이전 화면 꺼내서 보여줌
function goBack() {
    const previous = screenHistory.pop();
    if (previous) {
        switchScreen(previous);
    }
}

// 뒤로 버튼들 전부 goBack 함수 연결
document.querySelectorAll(".btn-back").forEach(btn => {
    btn.addEventListener("click", goBack);
});

//  3. 로딩 오버레이

function showLoading(text) {
    document.getElementById("loading-text").textContent = text;
    document.getElementById("loading-overlay").classList.remove("hidden");
}
function hideLoading() {
    document.getElementById("loading-overlay").classList.add("hidden");
}

//  4. Gemini API 호출 (AI 쓰는 함수는 여기 3개)

// 구글 AI 스튜디오에서 받은 API 키
// 배포하기 전에 구글 클라우드 콘솔에서 내 사이트 주소로 제한 걸어두기!
const GEMINI_API_KEY = "";

// 지금 쓰는 모델 이름
// 나중에 모델 없어졌다고 에러 뜨면 여기 이름만 그 에러에 나온 걸로 바꾸면 됨
const GEMINI_MODEL = "gemini-3.6-flash";

// Gemini한테 텍스트 보내고 응답 받아오는 함수
async function askGemini(systemPrompt, userMessage) {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${GEMINI_API_KEY}`;

    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            systemInstruction: { parts: [{ text: systemPrompt }] },
            contents: [{ parts: [{ text: userMessage }] }],
            generationConfig: { responseMimeType: "application/json" }
        })
    });

    if (!response.ok) {
        const errBody = await response.text();
        throw new Error("API 오류 (" + response.status + "): " + errBody);
    }

    const data = await response.json();
    const text = data.candidates[0].content.parts.map(part => part.text || "").join("");

    // 가끔 ```json 으로 감싸서 응답할 때가 있어서 벗겨내는 부분
    const cleaned = text.replace(/```json/g, "").replace(/```/g, "").trim();
    return JSON.parse(cleaned);
}

// (1) 신체정보 + 목표 넣으면 하루 권장 칼로리랑 탄단지 계산해줌
async function callAIForGoal(profile, goalType) {
    const presetLabels = { bulk: "벌크업(근육 증량)", diet: "다이어트(체중 감량)", health: "건강관리(체중 유지)" };
    const goalLabel = goalType === "custom" ? state.customGoalText : presetLabels[goalType];

    const system =
        "너는 영양 코치야. 사용자의 신체 정보와 목표를 보고 하루 권장 섭취량을 계산해. " +
        "다른 설명 없이 아래 형식의 JSON 객체 하나만 출력해: " +
        '{"calories": 숫자, "carb_g": 숫자, "protein_g": 숫자, "fat_g": 숫자}';

    const userMessage =
        `키 ${profile.height}cm, 몸무게 ${profile.weight}kg, 나이 ${profile.age}세, ` +
        `성별 ${profile.gender === "male" ? "남성" : "여성"}, 활동량 ${profile.activity}, ` +
        `목표는 ${goalLabel}. 하루 권장 칼로리와 탄수화물/단백질/지방 목표량(g)을 알려줘.`;

    return askGemini(system, userMessage);
}

// (2) 먹은 거 텍스트로 적으면 칼로리/탄단지 계산
async function callAIForMeal(mealText) {
    const system =
        "너는 영양 분석가야. 사용자가 적은 음식 설명을 보고 예상 영양 성분을 추정해. " +
        "다른 설명 없이 아래 형식의 JSON 객체 하나만 출력해: " +
        '{"food_name": "짧은 요약 이름", "calories": 숫자, "carb_g": 숫자, "protein_g": 숫자, "fat_g": 숫자}';

    return askGemini(system, mealText);
}

async function callAIForMealPhoto(base64Image, mimeType) {
    const system =
        "너는 영양 분석가야. 사용자가 올린 음식 사진을 보고 어떤 음식인지, 예상 영양 성분을 추정해. " +
        "다른 설명 없이 아래 형식의 JSON 객체 하나만 출력해: " +
        '{"food_name": "짧은 요약 이름", "calories": 숫자, "carb_g": 숫자, "protein_g": 숫자, "fat_g": 숫자}';

    const url = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${GEMINI_API_KEY}`;

    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            systemInstruction: { parts: [{ text: system }] },
            contents: [{
                parts: [
                    { inline_data: { mime_type: mimeType, data: base64Image } },
                    { text: "이 사진 속 음식의 영양 성분을 분석해줘." }
                ]
            }],
            generationConfig: { responseMimeType: "application/json" }
        })
    });

    if (!response.ok) {
        const errBody = await response.text();
        throw new Error("API 오류 (" + response.status + "): " + errBody);
    }

    const data = await response.json();
    const text = data.candidates[0].content.parts.map(part => part.text || "").join("");
    const cleaned = text.replace(/```json/g, "").replace(/```/g, "").trim();
    return JSON.parse(cleaned);
}

// 사진 파일 base64로 바꿔주는 함수
function readFileAsBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
            const base64 = reader.result.split(",")[1];
            resolve({ base64, mimeType: file.type });
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

// 5. 화면1 - 온보딩 (신체정보 입력)

document.getElementById("btn-to-goal").addEventListener("click", () => {
    state.profile = {
        height: document.getElementById("input-height").value,
        weight: document.getElementById("input-weight").value,
        age: document.getElementById("input-age").value,
        gender: document.getElementById("input-gender").value,
        activity: document.getElementById("input-activity").value
    };
    saveState();
    showScreen("screen-goal");
});


//  6. 화면2 - 목표 설정


// 목표 버튼 클릭했을 때
document.querySelectorAll("#goal-type-group .choice").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll("#goal-type-group .choice").forEach(b => b.classList.remove("selected"));
        btn.classList.add("selected");
        state.goalType = btn.dataset.goal;

        // 기타 선택했을 때만 입력창 보여주기
        document.getElementById("panel-custom-goal").classList.toggle("hidden", state.goalType !== "custom");
    });
});

// 기타 목표 직접 입력받기
document.getElementById("input-custom-goal").addEventListener("input", (e) => {
    state.customGoalText = e.target.value;
});

// AI자동/직접 선택하면 거기에 맞는 패널만 보여주기
document.querySelectorAll("#goal-method-group .choice").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll("#goal-method-group .choice").forEach(b => b.classList.remove("selected"));
        btn.classList.add("selected");
        state.goalMethod = btn.dataset.method;

        document.getElementById("panel-ai").classList.toggle("hidden", state.goalMethod !== "ai");
        document.getElementById("panel-manual").classList.toggle("hidden", state.goalMethod !== "manual");
    });
});

// AI 계산 버튼 눌렀을 때
document.getElementById("btn-calc-ai").addEventListener("click", async () => {
    const errBox = document.getElementById("goal-error");
    errBox.classList.add("hidden");

    if (!state.goalType) { showGoalError("먼저 목표(벌크업/다이어트/건강관리/기타)를 선택해주세요."); return; }
    if (state.goalType === "custom" && !state.customGoalText) { showGoalError("목표를 직접 입력해주세요."); return; }

    try {
        showLoading("AI가 영양소 목표를 계산하고 있어요…");
        const result = await callAIForGoal(state.profile, state.goalType);

        state.goal = {
            calories: result.calories,
            carb: result.carb_g,
            protein: result.protein_g,
            fat: result.fat_g
        };

        const preview = document.getElementById("ai-goal-preview");
        preview.innerHTML =
            `<div class="ledger-row ledger-strong"><span>칼로리</span><span class="dots"></span><span class="mono">${state.goal.calories} kcal</span></div>` +
            `<div class="ledger-row"><span><i class="dot dot-carb"></i>탄수화물</span><span class="dots"></span><span class="mono">${state.goal.carb} g</span></div>` +
            `<div class="ledger-row"><span><i class="dot dot-protein"></i>단백질</span><span class="dots"></span><span class="mono">${state.goal.protein} g</span></div>` +
            `<div class="ledger-row"><span><i class="dot dot-fat"></i>지방</span><span class="dots"></span><span class="mono">${state.goal.fat} g</span></div>`;
        preview.classList.remove("hidden");
    } catch (err) {
        showGoalError("계산에 실패했어요: " + err.message);
    } finally {
        hideLoading();
    }
});

function showGoalError(message) {
    const errBox = document.getElementById("goal-error");
    errBox.textContent = message;
    errBox.classList.remove("hidden");
}

// 목표 저장 버튼
document.getElementById("btn-to-home").addEventListener("click", () => {
    if (!state.goalType) { showGoalError("목표를 선택해주세요."); return; }
    if (state.goalType === "custom" && !state.customGoalText) { showGoalError("목표를 직접 입력해주세요."); return; }

    if (state.goalMethod === "manual") {
        state.goal = {
            calories: Number(document.getElementById("input-cal-manual").value) || 0,
            carb: Number(document.getElementById("input-carb-manual").value) || 0,
            protein: Number(document.getElementById("input-protein-manual").value) || 0,
            fat: Number(document.getElementById("input-fat-manual").value) || 0
        };
    }

    if (!state.goal.calories) {
        showGoalError("영양소 목표를 먼저 설정해주세요 (AI 자동 계산 또는 직접 입력).");
        return;
    }

    saveState();
    renderHome();
    showScreen("screen-home");
});

//  7. 화면3 - 홈 대시보드

function renderHome() {
    document.getElementById("today-date").textContent = "03 · " + state.date + " 기록";

    // 오늘 먹은 거 합계 구하기
    const sum = state.meals.reduce((acc, m) => {
        acc.calories += m.calories; acc.carb += m.carb; acc.protein += m.protein; acc.fat += m.fat;
        return acc;
    }, { calories: 0, carb: 0, protein: 0, fat: 0 });

    document.getElementById("stat-kcal-now").textContent = Math.round(sum.calories);
    document.getElementById("stat-kcal-goal").textContent = Math.round(state.goal.calories);

    document.getElementById("ledger-kcal").textContent = `${Math.round(sum.calories)} / ${Math.round(state.goal.calories)} kcal`;
    document.getElementById("ledger-carb").textContent = `${Math.round(sum.carb)} / ${Math.round(state.goal.carb)} g`;
    document.getElementById("ledger-protein").textContent = `${Math.round(sum.protein)} / ${Math.round(state.goal.protein)} g`;
    document.getElementById("ledger-fat").textContent = `${Math.round(sum.fat)} / ${Math.round(state.goal.fat)} g`;

    renderRing(sum);
    renderMealList();

    // 오늘 합계를 날짜별 기록에도 저장 (주간 평균 계산용)
    state.history[state.date] = sum;
    saveState();
    renderWeeklyAverage();
}

// 도넛 링 그래프 부분
// 오늘칼로리/목표칼로리 비율만큼 채우고, 그 안에서 탄단지 비율로 색 나눔
function renderRing(sum) {
    const r = 82;
    const circumference = 2 * Math.PI * r;

    const goalCal = state.goal.calories || 1;
    const filledLength = Math.min(sum.calories / goalCal, 1) * circumference;

    const carbCal = sum.carb * 4;
    const proteinCal = sum.protein * 4;
    const fatCal = sum.fat * 9;
    const totalMacroCal = carbCal + proteinCal + fatCal || 1;

    const carbLen = (carbCal / totalMacroCal) * filledLength;
    const proteinLen = (proteinCal / totalMacroCal) * filledLength;
    const fatLen = (fatCal / totalMacroCal) * filledLength;

    setSegment(".ring-carb", circumference, carbLen, 0);
    setSegment(".ring-protein", circumference, proteinLen, carbLen);
    setSegment(".ring-fat", circumference, fatLen, carbLen + proteinLen);
}

function setSegment(selector, circumference, length, offsetFromStart) {
    const el = document.querySelector(selector);
    el.style.strokeDasharray = `${length} ${circumference - length}`;
    el.style.strokeDashoffset = -offsetFromStart;
}

function renderMealList() {
    const list = document.getElementById("meal-list");
    const emptyRow = document.getElementById("meal-empty");

    list.querySelectorAll("li:not(#meal-empty)").forEach(li => li.remove());

    if (state.meals.length === 0) {
        emptyRow.classList.remove("hidden");
        return;
    }
    emptyRow.classList.add("hidden");

    state.meals.forEach((meal, index) => {
        const li = document.createElement("li");
        li.innerHTML =
            `<span class="meal-name">${meal.name}</span>` +
            `<span class="meal-kcal">${Math.round(meal.calories)} kcal</span>` +
            `<button class="btn-delete" data-index="${index}">✕</button>`;
        list.appendChild(li);
    });

    // 방금 만든 삭제 버튼들에 클릭 이벤트 연결
    list.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", () => {
            const index = Number(btn.dataset.index);
            state.meals.splice(index, 1); // 그 음식 하나만 배열에서 빼기
            saveState();
            renderHome();
        });
    });
}

// 최근 7일 중 기록이 있는 날짜들만 모아서 평균 계산
function getWeeklyAverage() {
    const days = [];
    for (let i = 0; i < 7; i++) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        days.push(d.toISOString().slice(0, 10));
    }

    const entries = days.map(d => state.history[d]).filter(Boolean);
    if (entries.length === 0) return null;

    const total = entries.reduce((acc, e) => {
        acc.calories += e.calories; acc.carb += e.carb; acc.protein += e.protein; acc.fat += e.fat;
        return acc;
    }, { calories: 0, carb: 0, protein: 0, fat: 0 });

    const n = entries.length;
    return { calories: total.calories / n, carb: total.carb / n, protein: total.protein / n, fat: total.fat / n, days: n };
}

function renderWeeklyAverage() {
    const avg = getWeeklyAverage();
    document.getElementById("weekly-days").textContent = avg ? avg.days : 0;
    document.getElementById("weekly-kcal").textContent = avg ? `${Math.round(avg.calories)} kcal` : "-";
    document.getElementById("weekly-carb").textContent = avg ? `${Math.round(avg.carb)} g` : "-";
    document.getElementById("weekly-protein").textContent = avg ? `${Math.round(avg.protein)} g` : "-";
    document.getElementById("weekly-fat").textContent = avg ? `${Math.round(avg.fat)} g` : "-";
}

document.getElementById("btn-reset-all").addEventListener("click", () => {
    if (confirm("신체 정보와 목표까지 전부 지우고 처음부터 다시 시작할까요?")) {
        localStorage.removeItem(STORAGE_KEY);
        location.reload();
    }
});

document.getElementById("btn-to-diet").addEventListener("click", () => {
    document.getElementById("input-meal-text").value = "";
    document.getElementById("input-meal-photo").value = "";
    document.getElementById("meal-photo-preview").classList.add("hidden");
    document.getElementById("diet-error").classList.add("hidden");
    showScreen("screen-diet");
});

document.getElementById("btn-reset-day").addEventListener("click", () => {
    if (confirm("오늘 기록한 식단을 모두 지울까요?")) {
        state.meals = [];
        saveState();
        renderHome();
    }
});

//  8. 화면4 - 식단 등록

let pendingMeal = null; // AI 분석 결과 잠깐 저장해두는 변수

// 사진 선택하면 미리보기 보여주기
document.getElementById("input-meal-photo").addEventListener("change", (e) => {
    const file = e.target.files[0];
    const preview = document.getElementById("meal-photo-preview");

    if (!file) {
        preview.classList.add("hidden");
        return;
    }
    preview.src = URL.createObjectURL(file);
    preview.classList.remove("hidden");
});

document.getElementById("btn-analyze-meal").addEventListener("click", async () => {
    const text = document.getElementById("input-meal-text").value.trim();
    const photoFile = document.getElementById("input-meal-photo").files[0];
    const errBox = document.getElementById("diet-error");
    errBox.classList.add("hidden");

    if (!text && !photoFile) {
        errBox.textContent = "먹은 음식을 텍스트로 입력하거나, 사진을 올려주세요.";
        errBox.classList.remove("hidden");
        return;
    }

    try {
        showLoading("AI가 영양소를 분석하고 있어요…");

        let result;
        let fallbackName;
        if (photoFile) {
            const { base64, mimeType } = await readFileAsBase64(photoFile);
            result = await callAIForMealPhoto(base64, mimeType);
            fallbackName = "사진으로 등록한 음식";
        } else {
            result = await callAIForMeal(text);
            fallbackName = text.slice(0, 20);
        }

        pendingMeal = {
            name: result.food_name || fallbackName,
            calories: Number(result.calories) || 0,
            carb: Number(result.carb_g) || 0,
            protein: Number(result.protein_g) || 0,
            fat: Number(result.fat_g) || 0
        };

        document.getElementById("result-food-name").value = pendingMeal.name;
        document.getElementById("result-kcal").value = Math.round(pendingMeal.calories);
        document.getElementById("result-carb").value = Math.round(pendingMeal.carb);
        document.getElementById("result-protein").value = Math.round(pendingMeal.protein);
        document.getElementById("result-fat").value = Math.round(pendingMeal.fat);

        showScreen("screen-result");
    } catch (err) {
        errBox.textContent = "분석에 실패했어요: " + err.message;
        errBox.classList.remove("hidden");
    } finally {
        hideLoading();
    }
});

document.getElementById("btn-cancel-meal").addEventListener("click", () => showScreen("screen-home"));


//  9. 화면5 - 분석 결과 확인

document.getElementById("btn-save-meal").addEventListener("click", () => {
    if (!pendingMeal) return;

    // 사용자가 저장 전에 직접 고친 값이 있으면 그 값을 그대로 반영
    pendingMeal.name = document.getElementById("result-food-name").value.trim() || pendingMeal.name;
    pendingMeal.calories = Number(document.getElementById("result-kcal").value) || 0;
    pendingMeal.carb = Number(document.getElementById("result-carb").value) || 0;
    pendingMeal.protein = Number(document.getElementById("result-protein").value) || 0;
    pendingMeal.fat = Number(document.getElementById("result-fat").value) || 0;

    state.meals.push(pendingMeal);
    pendingMeal = null;
    saveState();
    renderHome();
    showScreen("screen-home");
});

document.getElementById("btn-redo-meal").addEventListener("click", () => showScreen("screen-diet"));


//  10. 로드할 때 - 저장된 정보 있으면 이어서, 없으면 처음부터

loadState();

if (state.goal.calories > 0) {
    renderHome();
    showScreen("screen-home");
} else {
    // 예전에 입력한 값 있으면 다시 채워넣기
    if (state.profile.height) document.getElementById("input-height").value = state.profile.height;
    if (state.profile.weight) document.getElementById("input-weight").value = state.profile.weight;
    if (state.profile.age) document.getElementById("input-age").value = state.profile.age;
    showScreen("screen-onboarding");
}