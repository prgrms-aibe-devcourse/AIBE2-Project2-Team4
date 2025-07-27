// 전역 변수
let selectedMentoringId = null;
let selectedRating = 0;
let csrfToken = null;
let csrfHeader = null;

document.addEventListener('DOMContentLoaded', function() {
    // CSRF 토큰 안전하게 가져오기
    initializeCsrfTokens();

    // 페이지 초기화
    loadAvailableMentorings();
    setupStarRating();
    setupFormValidation();
});

// CSRF 토큰 초기화
function initializeCsrfTokens() {
    const csrfMetaToken = document.querySelector('meta[name="_csrf"]');
    const csrfMetaHeader = document.querySelector('meta[name="_csrf_header"]');

    if (csrfMetaToken && csrfMetaHeader) {
        csrfToken = csrfMetaToken.getAttribute('content');
        csrfHeader = csrfMetaHeader.getAttribute('content');
    } else {
        console.warn('CSRF 토큰을 찾을 수 없습니다.');
    }
}

// 작성 가능한 멘토링 목록 로드
function loadAvailableMentorings() {
    const container = document.getElementById('mentoringList');

    fetch(`${window.location.origin}/api/mentoring-review/available-mentorings`)
        .then(response => {
            if (response.status === 401) {
                throw new Error('로그인이 필요합니다.');
            }
            if (!response.ok) {
                throw new Error('멘토링 정보를 불러오지 못했습니다.');
            }
            return response.json();
        })
        .then(mentorings => {
            renderMentoringList(mentorings);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-warning">${error.message}</div>`;
        });
}

// 멘토링 목록 렌더링
function renderMentoringList(mentorings) {
    const container = document.getElementById('mentoringList');

    if (!Array.isArray(mentorings) || mentorings.length === 0) {
        container.innerHTML = '<p class="text-muted">작성 가능한 멘토링이 없습니다.</p>';
        return;
    }

    const mentoringHtml = mentorings.map(mentoring => `
        <div class="mentoring-option" onclick="selectMentoring(${mentoring.id}, this)">
            <div class="fw-bold">${mentoring.topic}</div>
            <div class="text-muted small">멘토: ${mentoring.mentorName || '-'}</div>
            <div class="text-muted small">일정: ${formatDate(mentoring.scheduledAt)}</div>
        </div>
    `).join('');

    container.innerHTML = mentoringHtml;
}

// 멘토링 선택
function selectMentoring(id, element) {
    // 기존 선택 해제
    document.querySelectorAll('.mentoring-option').forEach(el => {
        el.classList.remove('selected');
    });

    // 새로운 선택 적용
    element.classList.add('selected');
    selectedMentoringId = id;
    validateForm();
}

// 별점 기능 설정
function setupStarRating() {
    const stars = document.querySelectorAll('.star-input');
    const ratingText = document.getElementById('ratingText');
    const ratingTexts = ['', '별로예요', '보통이에요', '좋아요', '훌륭해요', '최고예요!'];

    stars.forEach((star, index) => {
        // 마우스 오버시 별점 하이라이트
        star.addEventListener('mouseover', () => {
            highlightStars(index + 1);
        });

        // 클릭시 별점 선택
        star.addEventListener('click', () => {
            selectedRating = index + 1;
            document.getElementById('rating').value = selectedRating;
            ratingText.textContent = ratingTexts[selectedRating];
            updateStarDisplay();
            validateForm();
        });
    });

    // 마우스가 별점 영역을 벗어나면 원래 상태로 복원
    const starContainer = document.getElementById('starRating');
    if (starContainer) {
        starContainer.addEventListener('mouseleave', updateStarDisplay);
    }
}

// 별점 하이라이트 표시
function highlightStars(count) {
    const stars = document.querySelectorAll('.star-input');
    stars.forEach((star, index) => {
        star.classList.toggle('hover', index < count);
    });
}

// 선택된 별점 표시 업데이트
function updateStarDisplay() {
    const stars = document.querySelectorAll('.star-input');
    stars.forEach((star, index) => {
        star.classList.remove('hover', 'active');
        if (index < selectedRating) {
            star.classList.add('active');
        }
    });
}

// 폼 유효성 검사 설정
function setupFormValidation() {
    const form = document.getElementById('reviewForm');
    const title = document.getElementById('title');
    const content = document.getElementById('content');

    // 입력값 변경시 유효성 검사
    [title, content].forEach(input => {
        if (input) {
            input.addEventListener('input', validateForm);
        }
    });

    // 폼 제출 이벤트 처리
    if (form) {
        form.addEventListener('submit', handleSubmit);
    }
}

// 폼 유효성 검사 실행
function validateForm() {
    const titleElement = document.getElementById('title');
    const contentElement = document.getElementById('content');
    const submitBtn = document.getElementById('submitBtn');

    if (!titleElement || !contentElement || !submitBtn) return;

    const title = titleElement.value.trim();
    const content = contentElement.value.trim();

    // 모든 필수 항목이 입력되었는지 확인
    const isValid = selectedMentoringId && selectedRating > 0 && title && content;
    submitBtn.disabled = !isValid;
}

// 폼 제출 처리
function handleSubmit(e) {
    e.preventDefault();

    const submitBtn = document.getElementById('submitBtn');
    if (!submitBtn) return;

    // 버튼 상태 변경
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>등록 중...';

    const formData = {
        mentoringRequestId: selectedMentoringId,
        title: document.getElementById('title').value.trim(),
        content: document.getElementById('content').value.trim(),
        rating: selectedRating
    };

    // 헤더 설정
    const headers = {
        'Content-Type': 'application/json'
    };

    // CSRF 토큰이 있으면 추가
    if (csrfHeader && csrfToken) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/api/mentoring-review', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(formData)
    })
        .then(response => {
            if (response.ok) return response.json();
            return response.text().then(text => {
                throw new Error(text);
            });
        })
        .then(data => {
            alert(data.message || '후기가 등록되었습니다!');
            window.location.href = '/mentoringReview';
        })
        .catch(error => {
            alert('등록 실패: ' + error.message);
            submitBtn.disabled = false;
            submitBtn.innerHTML = '후기 등록';
        });
}

// 날짜 포맷팅
function formatDate(dateString) {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('ko-KR');
}

// 뒤로가기
function goBack() {
    if (confirm('작성 중인 내용이 사라집니다. 정말 취소하시겠습니까?')) {
        window.location.href = '/mentoringReview';
    }
}