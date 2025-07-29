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
    'COMPLETED': '<span class="badge bg-dark text-white">완료</span>'
};

// 탭별 빈 상태 메시지 정의
function getEmptyMessage(tabType) {
    const messages = {
        'received': '받은 신청이 없습니다.',
        'sent': '보낸 신청이 없습니다.',
        'accepted': '진행 중인 멘토링이 없습니다.',
        'completed': '완료된 멘토링이 없습니다.',
        'mentors': '등록된 멘토가 없습니다.'
    };

    return messages[tabType] || '데이터가 없습니다.';
}

function loadTabContent(type) {
    const container = document.getElementById('mentoring-content');
    container.innerHTML = '<p class="text-muted">로딩 중...</p>';

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

            let mentors = data;
            let currentUserEmail = null;

            if (type === 'mentors' && data.mentors) {
                mentors = data.mentors;
                currentUserEmail = data.currentUserEmail;
            } else if (type === 'mentors' && Array.isArray(data)) {
                // 기존 형식 그대로인 경우 - 임시로 prompt로 현재 사용자 확인
                mentors = data;
            }

            if ((type === 'mentors' ? mentors : data).length === 0) {
                const emptyMessage = getEmptyMessage(type);
                container.innerHTML = `
                    <div class="text-center py-5">
                        <div class="text-muted mb-3">
                            <i class="fas fa-inbox fa-3x"></i>
                        </div>
                        <h5 class="text-muted">${emptyMessage}</h5>
                        <p class="text-muted small">새로운 활동을 시작해보세요!</p>
                    </div>
                `;
                return;
            }

            if (type === 'mentors') {
                mentors.forEach(item => {
                    const card = document.createElement('div');
                    card.className = 'card mb-3';

                    console.log('현재 사용자 이메일:', currentUserEmail);
                    console.log('멘토 이메일:', item.email);
                    console.log('같나?:', currentUserEmail === item.email);
                    // 현재 사용자인지 확인
                    const isMyProfile = currentUserEmail && item.email === currentUserEmail;

                    // 버튼 HTML
                    const buttonsHtml = isMyProfile ? `
                        <div class="mt-3">
                            <button class="btn btn-sm btn-outline-primary me-2" onclick="editProfile('${item.techStack.replace(/'/g, "\\'")}', '${item.description.replace(/'/g, "\\'")}')">
                                수정
                            </button>
                            <button class="btn btn-sm btn-outline-danger" onclick="deleteProfile()">
                                삭제
                            </button>
                        </div>
                    ` : '';

                    card.innerHTML = `
                        <div class="card-body">
                            <h5 class="card-title">${item.userName}</h5>
                            <p class="card-text">소개: ${item.description}</p>
                            <p class="card-text text-muted">주요 기술: ${item.techStack}</p>
                            ${buttonsHtml}
                        </div>
                    `;
                    container.appendChild(card);
                });
            } else {
                data.forEach(item => {
                    const card = document.createElement('a');
                    card.className = 'card mb-3 clickable-card';
                    card.href = `/mentoring/detail?id=${item.id}`;

                    // 클릭 이벤트 추가 - 어떤 탭에서 왔는지 저장
                    card.addEventListener('click', function(e) {
                        // 권한 정보 저장
                        if (type === 'received') {
                            sessionStorage.setItem('userRole', 'mentor'); // 받은 신청 = 멘토 권한
                        } else if (type === 'sent') {
                            sessionStorage.setItem('userRole', 'mentee'); // 보낸 신청 = 멘티 권한
                        } else {
                            sessionStorage.setItem('userRole', 'both'); // 수락됨/완료됨 = 둘 다
                        }
                        console.log('권한 저장:', sessionStorage.getItem('userRole'));
                    });

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

// 프로필 수정 함수
function editProfile(techStack, description) {
    const newTechStack = prompt('기술 스택:', techStack);
    if (newTechStack === null) return;

    const newDescription = prompt('소개글:', description);
    if (newDescription === null) return;

    if (!newTechStack.trim() || !newDescription.trim()) {
        alert('모든 필드를 입력해주세요.');
        return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    console.log('CSRF Token:', csrfToken);
    console.log('CSRF Header:', csrfHeader);

    const headers = {
        'Content-Type': 'application/json'
    };

    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/api/mentoring/profile', {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify({
            techStack: newTechStack.trim(),
            description: newDescription.trim()
        })
    })
        .then(response => {
            console.log('Response status:', response.status);
            if (response.ok) {
                alert('수정되었습니다.');
                loadTabContent('mentors');
            } else {
                return response.text().then(text => {
                    console.log('Error response:', text);
                    throw new Error(text);
                });
            }
        })
        .catch(error => {
            console.error('Fetch error:', error);
            alert('수정 실패: ' + error.message);
        });
}

// 프로필 삭제 함수
function deleteProfile() {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    console.log('CSRF Token:', csrfToken);
    console.log('CSRF Header:', csrfHeader);

    const headers = {};

    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/api/mentoring/profile', {
        method: 'DELETE',
        headers: headers
    })
        .then(response => {
            console.log('Response status:', response.status);
            if (response.ok) {
                alert('삭제되었습니다.');
                loadTabContent('mentors');
            } else {
                return response.text().then(text => {
                    console.log('Error response:', text);
                    throw new Error(text);
                });
            }
        })
        .catch(error => {
            console.error('Fetch error:', error);
            alert('삭제 실패: ' + error.message);
        });
}