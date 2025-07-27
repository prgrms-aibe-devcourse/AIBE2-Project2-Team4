let selectedMentoringId = null;
let selectedRating = 0;
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

document.addEventListener('DOMContentLoaded', function() {
    loadAvailableMentorings();
    setupStarRating();
    setupFormValidation();
});

function loadAvailableMentorings() {
    fetch(`${window.location.origin}/api/mentoring-review/available-mentorings`)
        .then(response => {
            if (response.status === 401) throw new Error('로그인이 필요합니다.');
            if (!response.ok) throw new Error('멘토링 정보를 불러오지 못했습니다.');
            return response.json();
        })
        .then(mentorings => renderMentoringList(mentorings))
        .catch(error => {
            document.getElementById('mentoringList').innerHTML =
                `<div class="alert alert-warning">${error.message}</div>`;
        });
}

function renderMentoringList(mentorings) {
    const container = document.getElementById('mentoringList');

    if (!Array.isArray(mentorings) || mentorings.length === 0) {
        container.innerHTML = '<p class="text-muted">작성 가능한 멘토링이 없습니다.</p>';
        return;
    }

    container.innerHTML = mentorings.map(mentoring => `
            <div class="mentoring-option" onclick="selectMentoring(${mentoring.id}, this)">
                <div class="fw-bold">${mentoring.topic}</div>
                <div class="text-muted small">멘토: ${mentoring.mentorName || '-'}</div>
                <div class="text-muted small">일정: ${formatDate(mentoring.scheduledAt)}</div>
            </div>
        `).join('');
}

function selectMentoring(id, element) {
    document.querySelectorAll('.mentoring-option').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
    selectedMentoringId = id;
    validateForm();
}

function setupStarRating() {
    const stars = document.querySelectorAll('.star-input');
    const ratingText = document.getElementById('ratingText');
    const ratingTexts = ['', '별로예요', '보통이에요', '좋아요', '훌륭해요', '최고예요!'];

    stars.forEach((star, index) => {
        star.addEventListener('mouseover', () => highlightStars(index + 1));
        star.addEventListener('click', () => {
            selectedRating = index + 1;
            document.getElementById('rating').value = selectedRating;
            ratingText.textContent = ratingTexts[selectedRating];
            updateStarDisplay();
            validateForm();
        });
    });

    document.getElementById('starRating').addEventListener('mouseleave', updateStarDisplay);
}

function highlightStars(count) {
    const stars = document.querySelectorAll('.star-input');
    stars.forEach((star, index) => {
        star.classList.toggle('hover', index < count);
    });
}

function updateStarDisplay() {
    const stars = document.querySelectorAll('.star-input');
    stars.forEach((star, index) => {
        star.classList.remove('hover', 'active');
        if (index < selectedRating) {
            star.classList.add('active');
        }
    });
}

function setupFormValidation() {
    const form = document.getElementById('reviewForm');
    const title = document.getElementById('title');
    const content = document.getElementById('content');

    [title, content].forEach(input => {
        input.addEventListener('input', validateForm);
    });

    form.addEventListener('submit', handleSubmit);
}

function validateForm() {
    const title = document.getElementById('title').value.trim();
    const content = document.getElementById('content').value.trim();
    const submitBtn = document.getElementById('submitBtn');

    const isValid = selectedMentoringId && selectedRating > 0 && title && content;
    submitBtn.disabled = !isValid;
}

function handleSubmit(e) {
    e.preventDefault();

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>등록 중...';

    const formData = {
        mentoringRequestId: selectedMentoringId,
        title: document.getElementById('title').value.trim(),
        content: document.getElementById('content').value.trim(),
        rating: selectedRating
    };

    fetch('/api/mentoring-review', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(formData)
    })
        .then(response => {
            if (response.ok) return response.json();
            return response.text().then(text => { throw new Error(text); });
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

function formatDate(dateString) {
    return dateString ? new Date(dateString).toLocaleDateString('ko-KR') : '';
}

function goBack() {
    if (confirm('작성 중인 내용이 사라집니다. 정말 취소하시겠습니까?')) {
        window.location.href = '/mentoringReview';
    }
}