document.addEventListener("DOMContentLoaded", () => {
    const requestId = new URLSearchParams(window.location.search).get('id');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    let sessionUrl = "", scheduledAt = "", proposedAt = "";
    let currentUserId = null, proposedById = null, currentStatus = "";

    // 멘토링 신청 정보 불러오기 - 에러 처리 추가
    fetch(`/api/mentoring/requests/${requestId}`)
        .then(res => {
            if (!res.ok) {
                if (res.status === 400) {
                    throw new Error("BLOCKED");
                }
                throw new Error("멘토링 신청을 불러올 수 없습니다.");
            }
            return res.json();
        })
        .then(data => {
            document.getElementById('mentorName').textContent = data.mentorName || '멘토 미지정';
            document.getElementById('userName').textContent = data.userName;
            document.getElementById('topic').textContent = data.topic;
            document.getElementById('message').textContent = data.message;
            document.getElementById('createdAt').textContent = data.createdAt;

            // 상태 배지 표시
            currentStatus = data.status;
            displayStatusBadge(currentStatus);

            sessionUrl = data.sessionUrl;
            scheduledAt = data.scheduledAt;
            proposedAt = data.proposedAt;
            currentUserId = data.currentUserId;
            proposedById = data.proposedById;

            console.log('현재 사용자 ID:', currentUserId);
            console.log('신청 상태:', currentStatus);

            showUIForStatus(currentStatus);
        })
        .catch(err => {
            if (err.message === "BLOCKED") {
                alert("삭제된 멘토링 신청입니다.");
                location.href = "/mentoring";
            } else {
                alert(err.message);
                location.href = "/mentoring";
            }
        });

    // 멘토링 신고 버튼 클릭 이벤트
    document.getElementById('reportMentoringBtn')?.addEventListener('click', function() {
        const reason = prompt('신고 사유를 입력해주세요.');
        if (!reason) return;

        fetch(`/api/report/mentoring/${requestId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ reason })
        })
            .then(res => {
                if (res.ok) {
                    alert('신고가 접수되었습니다.');
                } else {
                    throw new Error('신고 접수에 실패했습니다.');
                }
            })
            .catch(err => {
                alert(err.message);
            });
    });

    // 상태 배지 표시
    function displayStatusBadge(status) {
        const statusDisplay = document.getElementById('status-display');
        let badgeClass = '';
        let statusText = '';

        switch(status) {
            case 'PENDING':
                badgeClass = 'status-pending';
                statusText = '대기 중';
                break;
            case 'ACCEPTED':
                badgeClass = 'status-accepted';
                statusText = '수락됨';
                break;
            case 'COMPLETED':
                badgeClass = 'status-completed';
                statusText = '완료됨';
                break;
            case 'REJECTED':
                badgeClass = 'status-rejected';
                statusText = '거절됨';
                break;
        }

        statusDisplay.innerHTML = `<span class="status-badge ${badgeClass}">${statusText}</span>`;
    }

    // 상태별 UI 표시 (권한 체크 추가)
    function showUIForStatus(status) {
        if (status === 'PENDING') {
            // 멘토만 수락/거절 버튼을 볼 수 있음
            const userRole = sessionStorage.getItem('userRole');
            console.log('저장된 권한:', userRole);

            if (userRole === 'mentor') {
                document.getElementById('actionButtons').style.display = 'flex';
                console.log('멘토 권한: 수락/거절 버튼 표시');
            } else {
                console.log('멘티 권한: 수락/거절 버튼 숨김');
                // 멘티에게는 대기 중 메시지만 표시
                document.getElementById('actionButtons').style.display = 'none';
            }
        }
        else if (status === 'ACCEPTED') {
            if (!scheduledAt) {
                document.getElementById('schedule-section').style.display = 'block';

                // 제안된 일정이 있으면 표시
                if (proposedAt) {
                    const proposedInfo = document.createElement('div');
                    proposedInfo.className = 'alert alert-info mt-3';
                    proposedInfo.innerHTML = `
                        <h6><i class="fas fa-calendar-alt me-2"></i>제안된 일정</h6>
                        <p class="mb-0"><strong>제안된 멘토링 날짜:</strong> ${new Date(proposedAt).toLocaleString()}</p>
                    `;

                    // schedule-section 앞에 추가
                    document.getElementById('schedule-section').insertAdjacentElement('beforebegin', proposedInfo);
                }

                // 제안자는 수락 버튼 비활성화
                if (proposedAt && currentUserId !== proposedById) {
                    document.getElementById('confirmBtn').style.display = 'inline-block';
                }
            }

            if (scheduledAt && sessionUrl) {
                document.getElementById('confirmed-schedule').style.display = 'block';
                document.getElementById('scheduleDisplay').textContent = new Date(scheduledAt).toLocaleString();
                const sessionLink = document.getElementById('sessionLink');
                sessionLink.href = sessionUrl;
                sessionLink.textContent = sessionUrl;
            }

            // 완료 처리 버튼 표시
            document.getElementById('complete-section').style.display = 'block';
        }
        else if (status === 'COMPLETED') {
            // 완료된 멘토링 표시
            document.getElementById('completed-section').style.display = 'block';

            if (scheduledAt && sessionUrl) {
                document.getElementById('confirmed-schedule').style.display = 'block';
                document.getElementById('scheduleDisplay').textContent = new Date(scheduledAt).toLocaleString();
            }
        }
    }

    const now = new Date().toISOString().slice(0, 16);
    document.getElementById('scheduleDate').min = now;

    // 일정 제안
    document.getElementById('proposeBtn')?.addEventListener('click', () => {
        const value = document.getElementById('scheduleDate').value;
        if (!value) return alert("일정을 선택하세요.");
        fetch(`/api/mentoring/requests/${requestId}/propose`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ proposedAt: value })
        }).then(res => {
            if (!res.ok) throw new Error("일정 제안 실패");
            alert("일정이 제안되었습니다.");
            location.reload();
        }).catch(err => alert(err.message));
    });

    // 일정 확정
    document.getElementById('confirmBtn')?.addEventListener('click', () => {
        fetch(`/api/mentoring/requests/${requestId}/confirm`, {
            method: 'POST',
            headers: { [csrfHeader]: csrfToken }
        }).then(res => {
            if (!res.ok) throw new Error("확정 실패");
            return res.json();
        }).then(() => {
            alert("일정이 확정되었습니다.");
            location.reload();
        }).catch(err => alert(err.message));
    });

    // 멘토링 방 이동
    document.getElementById('goToRoomBtn')?.addEventListener('click', () => {
        if (sessionUrl) window.open(sessionUrl, '_blank');
    });
});

// 수락 함수
function accept() {
    const requestId = new URLSearchParams(window.location.search).get('id');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/api/mentoring/${requestId}/accept`, {
        method: 'POST',
        headers: { [csrfHeader]: csrfToken }
    }).then(res => {
        if (!res.ok) throw new Error("수락 실패");
        alert("수락되었습니다.");
        location.reload();
    }).catch(err => alert(err.message));
}

// 거절 함수
function reject() {
    const requestId = new URLSearchParams(window.location.search).get('id');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/api/mentoring/requests/${requestId}/reject`, {
        method: 'POST',
        headers: { [csrfHeader]: csrfToken }
    }).then(res => {
        if (!res.ok) throw new Error("거절 실패");
        alert("거절되었습니다.");
        location.href = "/mentoring";
    }).catch(err => alert(err.message));
}

// 상호 협의 후 멘토링 완료
function completeMentoring() {
    if (!confirm('멘토링을 완료 처리하시겠습니까?')) return;

    const requestId = new URLSearchParams(window.location.search).get('id');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/api/mentoring/${requestId}/complete`, {
        method: 'POST',
        headers: { [csrfHeader]: csrfToken }
    })
        .then(res => {
            if (res.ok) {
                return res.json();
            } else {
                return res.text().then(text => { throw new Error(text); });
            }
        })
        .then(data => {
            alert(data.message || '멘토링이 완료되었습니다.');
            location.reload();
        })
        .catch(err => {
            alert('완료 처리 실패: ' + err.message);
        });
}