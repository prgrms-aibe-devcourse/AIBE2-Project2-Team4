const urlParams = new URLSearchParams(window.location.search);
const reviewId = urlParams.get('id');

// 페이지 로드 시 후기 데이터 요청
document.addEventListener('DOMContentLoaded', () => {
    if (!reviewId) {
        showError('잘못된 접근입니다.');
        return;
    }
    loadReviewDetail();
});

function loadReviewDetail() {
    showLoading();
    fetch(`/api/mentoring-review/${reviewId}`)
        .then(res => {
            if (!res.ok) {
                if (res.status === 400) {
                    throw new Error('BLOCKED');
                }
                throw new Error('후기를 찾을 수 없습니다.');
            }
            return res.json();
        })
        .then(displayReview)
        .catch(err => {
            if (err.message === 'BLOCKED') {
                showError('삭제된 후기입니다.');
                setTimeout(() => {
                    window.location.href = '/mentoringReview';
                }, 2000);
            } else {
                showError(err.message);
            }
        });
}

// 멘토링 후기 신고 함수
function reportReview() {
    const reason = prompt('신고 사유를 입력해주세요.');
    if (!reason) return;

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    fetch(`/api/report/mentoring-review/${reviewId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {})
        },
        body: JSON.stringify({ reason })
    })
        .then(res => {
            if (res.ok) {
                alert('신고가 접수되었습니다.');
            } else {
                return res.text().then(text => {
                    throw new Error('신고 접수에 실패했습니다: ' + text);
                });
            }
        })
        .catch(err => {
            alert(err.message);
        });
}

// 후기 데이터 화면 출력
function displayReview(review) {
    document.getElementById('reviewTitle').textContent = review.title;
    document.getElementById('reviewerName').textContent = review.reviewerName;
    document.getElementById('mentorName').textContent = review.mentorName;
    document.getElementById('createdAt').textContent = formatDate(review.createdAt);
    document.getElementById('viewCount').textContent = review.viewCount;
    document.getElementById('reviewContent').textContent = review.content;

    document.getElementById('starRating').innerHTML = generateStarRating(review.rating);
    document.getElementById('ratingText').textContent = `${review.rating}점`;

    if (review.writer) {
        document.getElementById('ownerButtons').style.display = 'flex';
    }

    hideLoading();
    document.getElementById('reviewDetail').style.display = 'block';
}

function generateStarRating(rating) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        stars += `<span class="star${i <= rating ? ' filled' : ''}">★</span>`;
    }
    return stars;
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('ko-KR');
}

function showLoading() {
    document.getElementById('loadingState').style.display = 'block';
    document.getElementById('errorState').style.display = 'none';
    document.getElementById('reviewDetail').style.display = 'none';
}

function hideLoading() {
    document.getElementById('loadingState').style.display = 'none';
}

function showError(message) {
    hideLoading();
    document.getElementById('errorState').style.display = 'block';
    document.getElementById('reviewDetail').style.display = 'none';
    document.getElementById('errorMessage').textContent = message;
}

function goBack() {
    window.history.back();
}

function editReview() {
    window.location.href = `/mentoringReview/edit?id=${reviewId}`;
}

// 후기 삭제 요청
function deleteReview() {
    if (!confirm('정말로 이 후기를 삭제하시겠습니까?')) return;

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const headers = {
        'Content-Type': 'application/json',
        ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {})
    };

    fetch(`/api/mentoring-review/${reviewId}`, {
        method: 'DELETE',
        headers
    })
        .then(res => {
            if (!res.ok) {
                if (res.status === 400) {
                    throw new Error('삭제된 후기입니다.');
                }
                throw new Error('삭제 실패');
            }
            return res.json();
        })
        .then(data => {
            alert(data.message || '후기가 삭제되었습니다.');
            window.location.href = '/mentoringReview';
        })
        .catch(error => {
            if (error.message === '삭제된 후기입니다.') {
                alert(error.message);
                window.location.href = '/mentoringReview';
            } else {
                alert('삭제 실패: ' + error.message);
            }
        });
}