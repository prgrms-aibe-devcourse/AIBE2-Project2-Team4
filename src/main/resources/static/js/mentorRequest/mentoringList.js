document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('#mentoringTabs .nav-link').forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            document.querySelectorAll('#mentoringTabs .nav-link').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            loadTabContent(tab.dataset.type);
        });
    });
    loadTabContent('received');
});

const statusBadgeMap = {
    'PENDING': '<span class="badge bg-warning text-dark">대기중</span>',
    'ACCEPTED': '<span class="badge bg-success">수락됨</span>',
    'REJECTED': '<span class="badge bg-secondary">거절됨</span>',
    'COMPLETED': '<span class="badge bg-dark">완료</span>'
};

function loadTabContent(type) {
    const container = document.getElementById('mentoring-content');
    container.innerHTML = '<p>로딩 중...</p>';

    let url = '';
    switch (type) {
        case 'received':
            url = '/api/mentoring/requests/received';
            break;
        case 'sent':
            url = '/api/mentoring/requests/sent';
            break;
        case 'accepted':
            url = '/api/mentoring/requests/accepted';
            break;
        case 'completed':
            url = '/api/mentoring/requests/completed';
            break;
        case 'mentors':
            url = '/api/mentoring/mentors';
            break;
        default:
            container.innerHTML = '<p class="text-danger">잘못된 탭 유형입니다.</p>';
            return;
    }

    fetch(url)
        .then(res => res.json())
        .then(data => {
            container.innerHTML = '';

            if (data.length === 0) {
                container.innerHTML = '<p>표시할 데이터가 없습니다.</p>';
                return;
            }

            if (type === 'mentors') {
                data.forEach(item => {
                    const card = document.createElement('div');
                    card.className = 'card mb-3';
                    card.innerHTML = `
                            <div class="card-body">
                                <h5 class="card-title">${item.userName}</h5>
                                <p class="card-text">소개 : ${item.description}</p>
                                <p class="card-text text-muted">주요 기술: ${item.techStack}</p>
                            </div>
                        `;
                    container.appendChild(card);
                });
            } else {
                data.forEach(item => {
                    const card = document.createElement('a');
                    card.className = 'card mb-3 clickable-card';
                    card.href = `/mentoring/detail?id=${item.id}`;

                    const nameLine = type === 'received'
                        ? `<h5 class="card-title">멘티: ${item.userName}</h5>`
                        : `<h5 class="card-title">멘토: ${item.mentorName}</h5>`;

                    let extra = '';
                    if (item.scheduledAt) {
                        extra += `<p class="card-text">확정일: ${new Date(item.scheduledAt).toLocaleString()}</p>`;
                    }
                    if (item.sessionUrl) {
                        extra += `<a href="${item.sessionUrl}" target="_blank" class="btn btn-outline-secondary mt-2">멘토링 방으로 이동</a>`;
                    }

                    card.innerHTML = `
                            <div class="card-body">
                                ${nameLine}
                                <p class="card-text">주제: ${item.topic}</p>
                                <p class="card-text text-muted">신청일: ${new Date(item.createdAt).toISOString().slice(0, 10)}</p>
                                ${statusBadgeMap[item.status] || ''}
                                ${extra}
                            </div>
                        `;
                    container.appendChild(card);
                });
            }
        })
        .catch(err => {
            container.innerHTML = `<p class="text-danger">오류 발생: ${err.message}</p>`;
        });
}