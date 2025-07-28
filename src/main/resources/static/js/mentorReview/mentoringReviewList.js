let currentPage = 0;
let totalPages = 0;
let pageSize = 10;

document.addEventListener('DOMContentLoaded', () => {
    loadReviews();
});

function loadReviews(page = 0) {
    currentPage = page;
    const tbody = document.getElementById('reviewTableBody');

    tbody.innerHTML = `
        <tr>
            <td colspan="7" class="text-center">
                <div class="spinner-border spinner-border-sm me-2"></div>
                로딩 중...
            </td>
        </tr>
    `;

    fetch(`/api/mentoring-review/paged?page=${page}&size=${pageSize}`)
        .then(res => res.json())
        .then(data => {
            tbody.innerHTML = '';
            totalPages = data.totalPages || 0;
            const reviews = Array.isArray(data.reviews) ? data.reviews : [];

            if (reviews.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="7" class="text-center text-muted py-4">
                            아직 작성된 후기가 없습니다.
                        </td>
                    </tr>
                `;
                return;
            }

            reviews.forEach((review, index) => {
                const stars = generateStarRating(review.rating);
                const globalIndex = data.totalElements
                    ? data.totalElements - (page * pageSize) - index
                    : index + 1;

                const row = document.createElement('tr');
                row.style.cursor = 'pointer';
                row.onclick = () => viewReview(review.id);

                row.innerHTML = `
                    <td class="text-center">${globalIndex}</td>
                    <td>${review.reviewerName || '-'}</td>
                    <td>${review.mentorName || '-'}</td>
                    <td class="title-cell"><div class="review-title">${review.title}</div></td>
                    <td><div class="star-rating">${stars}</div></td>
                    <td class="text-center">${review.viewCount}</td>
                    <td>${formatDate(review.createdAt)}</td>
                `;
                tbody.appendChild(row);
            });

            updatePagination();
        })
        .catch(showError);
}

function loadMyReceivedReviews() {
    fetch(`/api/mentoring-review/mentor/me`)
        .then(res => res.json())
        .then(data => renderReviewList(data))
        .catch(showError);
}

function loadMyWrittenReviews() {
    fetch(`/api/mentoring-review/mentee/me`)
        .then(res => res.json())
        .then(data => renderReviewList(data))
        .catch(showError);
}

function renderReviewList(reviews) {
    const tbody = document.getElementById('reviewTableBody');
    const pagination = document.getElementById('pagination');
    tbody.innerHTML = '';
    pagination.style.display = 'none';

    if (!Array.isArray(reviews) || reviews.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-muted">
                    후기가 없습니다.
                </td>
            </tr>
        `;
        return;
    }

    reviews.forEach((review, index) => {
        const stars = generateStarRating(review.rating);
        const row = document.createElement('tr');
        row.onclick = () => viewReview(review.id);

        row.innerHTML = `
            <td class="text-center">${index + 1}</td>
            <td>${review.reviewerName || '-'}</td>
            <td>${review.mentorName || '-'}</td>
            <td class="title-cell"><div class="review-title">${review.title}</div></td>
            <td><div class="star-rating">${stars}</div></td>
            <td class="text-center">${review.viewCount}</td>
            <td>${formatDate(review.createdAt)}</td>
        `;
        tbody.appendChild(row);
    });
}

function updatePagination() {
    const pagination = document.getElementById('pagination');
    const pageNumbers = document.getElementById('pageNumbers');
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');

    if (totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }

    pagination.style.display = 'flex';
    prevPage.classList.toggle('disabled', currentPage === 0);
    nextPage.classList.toggle('disabled', currentPage === totalPages - 1);

    pageNumbers.innerHTML = '';
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        const pageItem = document.createElement('li');
        pageItem.className = `page-item ${i === currentPage ? 'active' : ''}`;

        const pageLink = document.createElement('a');
        pageLink.className = 'page-link';
        pageLink.href = '#';
        pageLink.textContent = i + 1;
        pageLink.onclick = (e) => {
            e.preventDefault();
            loadPage(i);
        };

        pageItem.appendChild(pageLink);
        pageNumbers.appendChild(pageItem);
    }
}

function loadPage(page) {
    if (page < 0 || page >= totalPages) return;
    loadReviews(page);
}

function generateStarRating(rating) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        stars += `<span class="star ${i <= rating ? 'filled' : ''}">★</span>`;
    }
    return stars;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        month: '2-digit',
        day: '2-digit'
    });
}

function viewReview(reviewId) {
    window.location.href = `/mentoringReview/detail?id=${reviewId}`;
}

function showError(err) {
    const tbody = document.getElementById('reviewTableBody');
    tbody.innerHTML = `
        <tr>
            <td colspan="7" class="text-center text-danger">
                오류 발생: ${err.message}
            </td>
        </tr>
    `;
}