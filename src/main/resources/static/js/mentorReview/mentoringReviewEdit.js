let selectedRating = 0;
let reviewId = null;
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    reviewId = urlParams.get('id');

    if (!reviewId) {
        showError('잘못된 접근입니다.');
        return;
    }

    loadReviewData();
    setupStarRating();
    setupFormValidation();
});

function loadReviewData() {
    showLoading();

    fetch(`/api/mentoring-review/${reviewId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('후기를 찾을 수 없습니다.');
            }
            return response.json();
        })
        .then(review => {
            document.getElementById('title').value = review.title;
            document.getElementById('content').value = review.content;
            document.getElementById('mentorName').textContent = review.mentorName;

            selectedRating = review.rating;
            document.getElementById('rating').value = selectedRating;
            updateStarDisplay();
            updateRatingText();

            hideLoading();
            document.getElementById('editForm').style.display = 'block';
            validateForm();
        })
        .catch(error => {
            showError(error.message);
        });
}

function setupStarRating() {
    const stars = document.querySelectorAll('.star-input');

    stars.forEach((star, index) => {
        star.addEventListener('mouseover', () => highlightStars(index + 1));
        star.addEventListener('click', () => {
            selectedRating = index + 1;
            document.getElementById('rating').value = selectedRating;
            updateStarDisplay();
            updateRatingText();
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

function updateRatingText() {
    const ratingTexts = ['', '별로예요', '보통이에요', '좋아요', '훌륭해요', '최고예요!'];
    document.getElementById('ratingText').textContent = ratingTexts[selectedRating];
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

    const isValid = selectedRating > 0 && title && content;
    submitBtn.disabled = !isValid;
}

function handleSubmit(e) {
    e.preventDefault();

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>수정 중...';

    const formData = {
        title: document.getElementById('title').value.trim(),
        content: document.getElementById('content').value.trim(),
        rating: selectedRating
    };

    fetch(`/api/mentoring-review/${reviewId}`, {
        method: 'PUT',
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
            alert(data.message || '후기가 수정되었습니다!');
            window.location.href = `/mentoringReview/detail?id=${reviewId}`;
        })
        .catch(error => {
            alert('수정 실패: ' + error.message);
            submitBtn.disabled = false;
            submitBtn.innerHTML = '수정 완료';
        });
}

function showLoading() {
    document.getElementById('loadingState').style.display = 'block';
    document.getElementById('errorState').style.display = 'none';
    document.getElementById('editForm').style.display = 'none';
}

function hideLoading() {
    document.getElementById('loadingState').style.display = 'none';
}

function showError(message) {
    document.getElementById('loadingState').style.display = 'none';
    document.getElementById('errorState').style.display = 'block';
    document.getElementById('editForm').style.display = 'none';
    document.getElementById('errorMessage').textContent = message;
}

function goBack() {
    if (confirm('수정 중인 내용이 사라집니다. 정말 취소하시겠습니까?')) {
        window.history.back();
    }
}