/* balanced diet - script.js
   화면(프론트엔드)만 담당. 저장과 AI 분석은 전부 Spring 서버 API가 처리함.
   브라우저에는 API 키도, 저장된 데이터도 없음.

   사용하는 서버 API
   - GET    /api/profile                  신체 정보·목표 조회 (없으면 404)
   - POST   /api/profile                  신체 정보 저장
   - POST   /api/goal/calculate           저장된 신체 정보로 AI 목표 계산
   - POST   /api/profile/goal             목표 저장
   - POST   /api/analyze                  식단 분석 (텍스트 또는 사진)
   - POST   /api/meals                    식단 저장
   - GET    /api/meals?date=YYYY-MM-DD    날짜별 식단 조회
   - DELETE /api/meals/{id}               식단 삭제
   - GET    /api/meals/weekly-average     최근 7일 평균
   ========================================================= */


//  1. 화면용 상태 — 화면을 그리려고 잠깐 들고 있는 값 (저장은 서버가 함)

let state = {
    goalType: "",         // "bulk" | "diet" | "health" | "custom"
    customGoalText: "",   // 기타를 골랐을 때 직접 쓴 목표
    goalMethod: "",       // "ai" | "manual"
    goal: {calories: 0, carb: 0, protein: 0, fat: 0},
    meals: []             // 오늘 먹은 음식 (서버에서 받아온 MealResponse 목록)
};

let pendingMeal = null;   // AI 분석 결과를 저장 전까지 잠깐 들고 있는 곳

// 오늘 날짜를 "YYYY-MM-DD"로 (브라우저가 있는 곳의 날짜 기준)
function getTodayString() {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, "0");
    const d = String(now.getDate()).padStart(2, "0");
    return `${y}-${m}-${d}`;
}


//  2. 화면 전환, 뒤로가기

let screenHistory = [];

function getCurrentScreenId() {
    const current = document.querySelector(".screen.active");
    return current ? current.id : null;
}

function switchScreen(id) {
    document.querySelectorAll(".screen").forEach(el => el.classList.remove("active"));
    document.getElementById(id).classList.add("active");
}

function showScreen(id) {
    const current = getCurrentScreenId();
    if (current && current !== id) {
        screenHistory.push(current);
    }
    switchScreen(id);
}

function goBack() {
    const previous = screenHistory.pop();
    if (previous) {
        switchScreen(previous);
    }
}

document.querySelectorAll(".btn-back").forEach(btn => {
    btn.addEventListener("click", goBack);
});


//  3. 로딩, 에러 표시

function showLoading(text) {
    document.getElementById("loading-text").textContent = text;
    document.getElementById("loading-overlay").classList.remove("hidden");
}

function hideLoading() {
    document.getElementById("loading-overlay").classList.add("hidden");
}

function showError(elementId, message) {
    const box = document.getElementById(elementId);
    box.textContent = message;
    box.classList.remove("hidden");
}

function hideError(elementId) {
    document.getElementById(elementId).classList.add("hidden");
}


//  4. 서버 API 호출

// 우리 서버 API를 부르는 공통 함수
// 실패하면 서버가 보낸 ErrorResponse의 message로 에러를 던짐
async function callApi(method, url, body) {
    const options = {method: method};

    if (body) {
        options.headers = {"Content-Type": "application/json"};
        options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);

    if (!response.ok) {
        const errorText = await response.text();
        let message = "요청에 실패했어요 (" + response.status + ")";
        if (errorText) {
            try {
                message = JSON.parse(errorText).message || message;
            } catch (e) {
                // JSON이 아닌 응답이면 기본 문장 사용
            }
        }
        throw new Error(message);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

// 사진을 가로·세로 최대 1024px로 줄인 뒤 base64 문자열로 바꿈
// 원본 그대로 보내면 용량이 커서 느리고, AI 사용량도 많이 듦
function readImageAsBase64(file, maxSize = 1024) {
    return new Promise((resolve, reject) => {
        const url = URL.createObjectURL(file);
        const img = new Image();

        img.onload = () => {
            const scale = Math.min(1, maxSize / Math.max(img.width, img.height));
            const canvas = document.createElement("canvas");
            canvas.width = Math.round(img.width * scale);
            canvas.height = Math.round(img.height * scale);
            canvas.getContext("2d").drawImage(img, 0, 0, canvas.width, canvas.height);
            URL.revokeObjectURL(url);

            const dataUrl = canvas.toDataURL("image/jpeg", 0.85);
            resolve({base64: dataUrl.split(",")[1], mimeType: "image/jpeg"});
        };

        img.onerror = () => {
            URL.revokeObjectURL(url);
            reject(new Error("사진을 읽지 못했어요. 다른 사진으로 시도해주세요."));
        };

        img.src = url;
    });
}


//  5. 화면1 - 온보딩 (신체 정보)

document.getElementById("btn-to-goal").addEventListener("click", async () => {
    hideError("onboarding-error");

    const profile = {
        height: Number(document.getElementById("input-height").value),
        weight: Number(document.getElementById("input-weight").value),
        age: Number(document.getElementById("input-age").value),
        gender: document.getElementById("input-gender").value,
        activity: document.getElementById("input-activity").value
    };

    try {
        await callApi("POST", "/api/profile", profile);
        showScreen("screen-goal");
    } catch (err) {
        showError("onboarding-error", err.message);
    }
});


//  6. 화면2 - 목표 설정

// 목표 종류 버튼
document.querySelectorAll("#goal-type-group .choice").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll("#goal-type-group .choice").forEach(b => b.classList.remove("selected"));
        btn.classList.add("selected");
        state.goalType = btn.dataset.goal;

        document.getElementById("panel-custom-goal").classList.toggle("hidden", state.goalType !== "custom");
    });
});

// 기타 목표 입력
document.getElementById("input-custom-goal").addEventListener("input", (e) => {
    state.customGoalText = e.target.value;
});

// AI 자동 / 직접 설정 선택
document.querySelectorAll("#goal-method-group .choice").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll("#goal-method-group .choice").forEach(b => b.classList.remove("selected"));
        btn.classList.add("selected");
        state.goalMethod = btn.dataset.method;

        document.getElementById("panel-ai").classList.toggle("hidden", state.goalMethod !== "ai");
        document.getElementById("panel-manual").classList.toggle("hidden", state.goalMethod !== "manual");
    });
});

// AI 계산 버튼 — 목표 종류만 보내면 서버가 저장된 신체 정보로 계산
document.getElementById("btn-calc-ai").addEventListener("click", async () => {
    hideError("goal-error");

    if (!state.goalType) {
        showError("goal-error", "먼저 목표(벌크업/다이어트/건강관리/기타)를 선택해주세요.");
        return;
    }
    if (state.goalType === "custom" && !state.customGoalText.trim()) {
        showError("goal-error", "목표를 직접 입력해주세요.");
        return;
    }

    try {
        showLoading("AI가 영양소 목표를 계산하고 있어요…");

        const result = await callApi("POST", "/api/goal/calculate", {
            goalType: state.goalType,
            customGoalText: state.customGoalText
        });

        state.goal = {
            calories: result.calories,
            carb: result.carb,
            protein: result.protein,
            fat: result.fat
        };

        const preview = document.getElementById("ai-goal-preview");
        preview.innerHTML =
            `<div class="ledger-row ledger-strong"><span>칼로리</span><span class="dots"></span><span class="mono">${state.goal.calories} kcal</span></div>` +
            `<div class="ledger-row"><span><span class="dot dot-carb"></span>탄수화물</span><span class="dots"></span><span class="mono">${state.goal.carb} g</span></div>` +
            `<div class="ledger-row"><span><span class="dot dot-protein"></span>단백질</span><span class="dots"></span><span class="mono">${state.goal.protein} g</span></div>` +
            `<div class="ledger-row"><span><span class="dot dot-fat"></span>지방</span><span class="dots"></span><span class="mono">${state.goal.fat} g</span></div>`;
        preview.classList.remove("hidden");
    } catch (err) {
        showError("goal-error", err.message);
    } finally {
        hideLoading();
    }
});

// 목표 저장 버튼
document.getElementById("btn-to-home").addEventListener("click", async () => {
    hideError("goal-error");

    if (!state.goalType) {
        showError("goal-error", "목표를 선택해주세요.");
        return;
    }
    if (state.goalType === "custom" && !state.customGoalText.trim()) {
        showError("goal-error", "목표를 직접 입력해주세요.");
        return;
    }

    if (state.goalMethod === "manual") {
        state.goal = {
            calories: Number(document.getElementById("input-cal-manual").value) || 0,
            carb: Number(document.getElementById("input-carb-manual").value) || 0,
            protein: Number(document.getElementById("input-protein-manual").value) || 0,
            fat: Number(document.getElementById("input-fat-manual").value) || 0
        };
    }

    if (!state.goal.calories) {
        showError("goal-error", "영양소 목표를 먼저 설정해주세요 (AI 자동 계산 또는 직접 입력).");
        return;
    }

    try {
        await callApi("POST", "/api/profile/goal", {
            goalType: state.goalType,
            customGoalText: state.customGoalText,
            calories: state.goal.calories,
            carb: state.goal.carb,
            protein: state.goal.protein,
            fat: state.goal.fat
        });
    } catch (err) {
        showError("goal-error", err.message);
        return;
    }

    await renderHome();
    showScreen("screen-home");
});


//  7. 화면3 - 홈

// 오늘 기록과 주간 평균을 서버에서 받아와서 화면을 다시 그림
async function renderHome() {
    hideError("home-error");
    const today = getTodayString();
    document.getElementById("today-date").textContent = "03 · " + today + " 기록";

    try {
        state.meals = await callApi("GET", "/api/meals?date=" + today);
    } catch (err) {
        state.meals = [];
        showError("home-error", "오늘 기록을 불러오지 못했어요: " + err.message);
    }

    // 오늘 먹은 것 합계
    const sum = state.meals.reduce((acc, m) => {
        acc.calories += m.calories;
        acc.carb += m.carb;
        acc.protein += m.protein;
        acc.fat += m.fat;
        return acc;
    }, {calories: 0, carb: 0, protein: 0, fat: 0});

    document.getElementById("stat-kcal-now").textContent = Math.round(sum.calories);
    document.getElementById("stat-kcal-goal").textContent = Math.round(state.goal.calories);

    document.getElementById("ledger-kcal").textContent = `${Math.round(sum.calories)} / ${Math.round(state.goal.calories)} kcal`;
    document.getElementById("ledger-carb").textContent = `${Math.round(sum.carb)} / ${Math.round(state.goal.carb)} g`;
    document.getElementById("ledger-protein").textContent = `${Math.round(sum.protein)} / ${Math.round(state.goal.protein)} g`;
    document.getElementById("ledger-fat").textContent = `${Math.round(sum.fat)} / ${Math.round(state.goal.fat)} g`;

    renderRing(sum);
    renderMealList();
    await renderWeeklyAverage();
}

// 도넛 링 — 목표 대비 먹은 칼로리만큼 채우고, 그 안을 탄단지 비율로 나눔
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

// 오늘 먹은 음식 목록 — 음식 이름은 사용자가 입력한 값이라 textContent로 넣음 (HTML로 해석되지 않게)
function renderMealList() {
    const list = document.getElementById("meal-list");
    const emptyRow = document.getElementById("meal-empty");

    list.querySelectorAll("li:not(#meal-empty)").forEach(li => li.remove());

    if (state.meals.length === 0) {
        emptyRow.classList.remove("hidden");
        return;
    }
    emptyRow.classList.add("hidden");

    state.meals.forEach(meal => {
        const li = document.createElement("li");

        const name = document.createElement("span");
        name.className = "meal-name";
        name.textContent = meal.name;

        const kcal = document.createElement("span");
        kcal.className = "meal-kcal";
        kcal.textContent = Math.round(meal.calories) + " kcal";

        const del = document.createElement("button");
        del.className = "btn-delete";
        del.textContent = "✕";
        del.setAttribute("aria-label", meal.name + " 삭제");
        del.addEventListener("click", async () => {
            try {
                await callApi("DELETE", "/api/meals/" + meal.id);
            } catch (err) {
                showError("home-error", err.message);
                return;
            }
            await renderHome();
        });

        li.append(name, kcal, del);
        list.appendChild(li);
    });
}

// 주간 평균 — 계산은 서버가 함
async function renderWeeklyAverage() {
    let avg = null;
    try {
        avg = await callApi("GET", "/api/meals/weekly-average");
    } catch (err) {
        avg = null;
    }

    const hasRecord = avg && avg.days > 0;
    document.getElementById("weekly-days").textContent = hasRecord ? avg.days : 0;
    document.getElementById("weekly-kcal").textContent = hasRecord ? `${avg.calories} kcal` : "-";
    document.getElementById("weekly-carb").textContent = hasRecord ? `${avg.carb} g` : "-";
    document.getElementById("weekly-protein").textContent = hasRecord ? `${avg.protein} g` : "-";
    document.getElementById("weekly-fat").textContent = hasRecord ? `${avg.fat} g` : "-";
}

document.getElementById("btn-to-diet").addEventListener("click", () => {
    document.getElementById("input-meal-text").value = "";
    document.getElementById("input-meal-photo").value = "";
    document.getElementById("meal-photo-preview").classList.add("hidden");
    hideError("diet-error");
    showScreen("screen-diet");
});

// 오늘 기록 초기화 — 오늘 기록을 하나씩 서버에서 삭제
document.getElementById("btn-reset-day").addEventListener("click", async () => {
    if (!confirm("오늘 기록한 식단을 모두 지울까요?")) return;

    try {
        for (const meal of state.meals) {
            await callApi("DELETE", "/api/meals/" + meal.id);
        }
    } catch (err) {
        showError("home-error", err.message);
    }
    await renderHome();
});

// 신체 정보·목표 다시 설정 — 서버가 "있으면 수정"으로 저장하니 지울 필요 없이 온보딩으로
document.getElementById("btn-reset-all").addEventListener("click", () => {
    hideError("onboarding-error");
    showScreen("screen-onboarding");
});


//  8. 화면4 - 식단 등록

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

// 분석 버튼 — 사진이 있으면 사진으로, 없으면 텍스트로 서버에 분석 요청
document.getElementById("btn-analyze-meal").addEventListener("click", async () => {
    const text = document.getElementById("input-meal-text").value.trim();
    const photoFile = document.getElementById("input-meal-photo").files[0];
    hideError("diet-error");

    if (!text && !photoFile) {
        showError("diet-error", "먹은 음식을 텍스트로 입력하거나, 사진을 올려주세요.");
        return;
    }

    try {
        showLoading("AI가 영양소를 분석하고 있어요…");

        let body;
        let fallbackName;
        if (photoFile) {
            const {base64, mimeType} = await readImageAsBase64(photoFile);
            body = {imageBase64: base64, mimeType: mimeType};
            fallbackName = "사진으로 등록한 음식";
        } else {
            body = {text: text};
            fallbackName = text.slice(0, 20);
        }

        const result = await callApi("POST", "/api/analyze", body);

        pendingMeal = {
            name: result.foodName || fallbackName,
            calories: Number(result.calories) || 0,
            carb: Number(result.carb) || 0,
            protein: Number(result.protein) || 0,
            fat: Number(result.fat) || 0
        };

        document.getElementById("result-food-name").value = pendingMeal.name;
        document.getElementById("result-kcal").value = Math.round(pendingMeal.calories);
        document.getElementById("result-carb").value = Math.round(pendingMeal.carb);
        document.getElementById("result-protein").value = Math.round(pendingMeal.protein);
        document.getElementById("result-fat").value = Math.round(pendingMeal.fat);

        hideError("result-error");
        showScreen("screen-result");
    } catch (err) {
        showError("diet-error", err.message);
    } finally {
        hideLoading();
    }
});

document.getElementById("btn-cancel-meal").addEventListener("click", () => showScreen("screen-home"));


//  9. 화면5 - 분석 결과 확인 후 저장

document.getElementById("btn-save-meal").addEventListener("click", async () => {
    if (!pendingMeal) return;
    hideError("result-error");

    // 사용자가 고친 값이 있으면 그 값으로 저장
    const meal = {
        name: document.getElementById("result-food-name").value.trim() || pendingMeal.name,
        calories: Number(document.getElementById("result-kcal").value) || 0,
        carb: Number(document.getElementById("result-carb").value) || 0,
        protein: Number(document.getElementById("result-protein").value) || 0,
        fat: Number(document.getElementById("result-fat").value) || 0
    };

    try {
        await callApi("POST", "/api/meals", meal);
    } catch (err) {
        showError("result-error", err.message);
        return;
    }

    pendingMeal = null;
    await renderHome();
    showScreen("screen-home");
});

document.getElementById("btn-redo-meal").addEventListener("click", () => showScreen("screen-diet"));


//  10. 처음 열 때 — 서버에 물어보고 어느 화면부터 보여줄지 정함

async function start() {
    // 1차 버전이 남긴 브라우저 저장 데이터 정리 (이제는 서버에 저장함)
    localStorage.removeItem("balancedDietState");

    let profile;
    try {
        profile = await callApi("GET", "/api/profile");
    } catch (err) {
        // 신체 정보가 없으면(404) 온보딩부터
        showScreen("screen-onboarding");
        return;
    }

    // 저장된 신체 정보를 입력칸에 채워두기 (다시 설정할 때 편하게)
    document.getElementById("input-height").value = profile.height;
    document.getElementById("input-weight").value = profile.weight;
    document.getElementById("input-age").value = profile.age;
    document.getElementById("input-gender").value = profile.gender;
    document.getElementById("input-activity").value = profile.activity;

    // 목표까지 정했으면 홈으로
    if (profile.goalCalories > 0) {
        state.goalType = profile.goalType || "";
        state.customGoalText = profile.customGoalText || "";
        state.goal = {
            calories: profile.goalCalories,
            carb: profile.goalCarb,
            protein: profile.goalProtein,
            fat: profile.goalFat
        };
        await renderHome();
        showScreen("screen-home");
        return;
    }

    // 신체 정보만 있으면 목표 설정부터
    showScreen("screen-goal");
}

start();