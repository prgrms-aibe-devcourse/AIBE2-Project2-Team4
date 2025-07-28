// 페이지 로드 시 탭 이벤트 리스너 등록 및 초기 탭 로드
document.addEventListener('DOMContentLoaded', () => {
    // 각 탭 클릭 이벤트 처리
    document.querySelectorAll('#studyTabs .nav-link').forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            // 기존 탭 제거
            document.querySelectorAll('#studyTabs .nav-link').forEach(t => t.classList.remove('active'));
            // 클릭한 탭 활성화
            tab.classList.add('active');
            // 해당 탭 내용 로드
            loadTabContent(tab.dataset.type);
        });
    });
    // 페이지 로드 시 기본적으로 모집 중
    loadTabContent('recruiting');
});

// 스터디 상태별 태그 스타일 정의
const statusTagMap = {
    true: '<span class="badge bg-secondary">모집마감</span>',
    false: '<span class="badge bg-success">모집중</span>'
};

// 완료된 스터디용 태그 정의
const completedTag = '<span class="badge bg-info">종료됨</span>';

// 탭별 빈 상태 메시지 정의
function getEmptyMessage(tabType) {
    const messages = {
        'recruiting': '모집 중인 스터디가 없습니다.',
        'closed': '모집 마감된 스터디가 없습니다.',
        'completed': '종료된 스터디가 없습니다.',
        'my': '만든 스터디가 없습니다.',
        'applied': '신청한 스터디가 없습니다.'
    };

    return messages[tabType] || '스터디가 없습니다.';
}

// 빈 상태일 때 메시지
function displayEmptyState(container, tabType) {
    const emptyMessage = getEmptyMessage(tabType);
    container.innerHTML = `
        <div class="text-center py-4 text-muted">
            <p class="mb-0">${emptyMessage}</p>
        </div>
    `;
}

// 선택된 탭에 따라 스터디 목록 데이터
function loadTabContent(type) {
    const container = document.getElementById('study-content');
    // 로딩 상태 표시
    container.innerHTML = '<p class="text-muted">로딩 중...</p>';

    // 탭 타입에 따른 API URL
    let url = '/api/studies/tab-studies?tab=' + type;

    // 서버에서 스터디 목록 데이터 요청
    fetch(url)
        .then(res => res.json())
        .then(data => {
            // 이전 내용 초기화
            container.innerHTML = '';

            // 데이터가 없는 경우 빈 상태 메시지 표시
            if (!data || data.length === 0) {
                displayEmptyState(container, type);
                return;
            }

            // 각 스터디 데이터를 카드로 렌더링
            data.forEach(study => {
                // 카드 생성
                const card = document.createElement('a');
                card.className = 'card mb-3 p-3 clickable-card';
                card.href = `/study/${study.id}`;
                card.style.display = 'block';

                // 모집 인원 정보 구성
                const recruitInfo = [];
                if (study.backendRecruit > 0) recruitInfo.push(`백엔드 ${study.backendRecruit}명`);
                if (study.frontendRecruit > 0) recruitInfo.push(`프론트엔드 ${study.frontendRecruit}명`);
                if (study.designerRecruit > 0) recruitInfo.push(`디자이너 ${study.designerRecruit}명`);
                if (study.plannerRecruit > 0) recruitInfo.push(`기획자 ${study.plannerRecruit}명`);

                // 기술스택 정보 HTML 구성
                let techStackHtml = '';
                if (study.techStacks && study.techStacks.length > 0) {
                    const techList = study.techStacks.join(', ');
                    techStackHtml = `<p class="card-text text-muted">기술스택: ${techList}</p>`;
                }

                // 작성자 정보 처리 (null 체크)
                const userName = study.user ? study.user.name : '알 수 없음';

                // 스터디 상태에 따른 태그 HTML 구성
                let tagHtml = '';
                if (study.completed) {
                    // 완료된 스터디
                    tagHtml = completedTag;
                } else {
                    // 모집중 또는 모집마감 스터디
                    tagHtml = statusTagMap[study.closed] || '';
                }

                // 카드 내용 구성
                card.innerHTML = `
        <div class="card-body">
            <h5 class="card-title">${study.title}</h5>
            <p class="card-text">${study.content}</p>
            <p class="card-text text-muted">작성자: ${userName}</p>
            <p class="card-text text-muted">마감일: ${new Date(study.deadline).toLocaleDateString()}</p>
            <p class="card-text text-muted">모집: ${recruitInfo.join(', ')}</p>
            ${techStackHtml}
            ${tagHtml}

            <button class="btn btn-sm btn-danger mt-2" onclick="event.preventDefault(); deleteStudy(${study.id});">삭제 (테스트용)</button>
            <button class="btn btn-sm btn-dark mt-2 ms-2" onclick="event.preventDefault(); completeStudy(${study.id});">스터디 완료처리 (테스트용)</button>
        </div>
    `;
                // 컨테이너에 카드 추가
                container.appendChild(card);
            });
        })
        .catch(err => {
            // 에러 발생 시 에러 메시지 표시
            console.error('Error:', err);
            container.innerHTML = `<p class="text-danger text-center py-4">오류 발생: ${err.message}</p>`;
        });
}

// 스터디 삭제 기능 (테스트용)
function deleteStudy(studyId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    // CSRF 토큰 가져오기
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // 삭제 요청
    fetch(`/api/studies/${studyId}`, {
        method: 'DELETE',
        headers: {
            [header]: token
        }
    })
        .then(res => {
            if (res.ok) {
                alert('삭제되었습니다.');
                // 현재 활성 탭 다시 로드
                loadTabContent(document.querySelector('#studyTabs .nav-link.active').dataset.type);
            } else {
                return res.text().then(t => { throw new Error(t); });
            }
        })
        .catch(err => alert('삭제 실패: ' + err.message));
}

// 스터디 강제 마감 처리 기능 (테스트용)
function forceCloseStudy(studyId) {
    if (!confirm('이 스터디를 강제로 모집마감 처리할까요?')) return;

    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/api/studies/${studyId}/force-close`, {
        method: 'POST',
        headers: {
            [header]: token
        }
    })
        .then(res => {
            if (res.ok) {
                alert('스터디가 마감 처리되었습니다.');
                loadTabContent(document.querySelector('#studyTabs .nav-link.active').dataset.type);
            } else {
                return res.text().then(t => { throw new Error(t); });
            }
        })
        .catch(err => alert('처리 실패: ' + err.message));
}

// 스터디 완료 처리 기능 (테스트용)
function completeStudy(studyId) {
    if (!confirm('이 스터디를 실제 완료 상태로 처리할까요?')) return;

    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/api/studies/${studyId}/force-complete`, {
        method: 'POST',
        headers: {
            [header]: token
        }
    })
        .then(res => {
            if (res.ok) {
                alert('스터디가 완료 처리되었습니다.');
                loadTabContent(document.querySelector('#studyTabs .nav-link.active').dataset.type);
            } else {
                return res.text().then(t => { throw new Error(t); });
            }
        })
        .catch(err => alert('처리 실패: ' + err.message));
}

// 마감된 스터디들 일괄 종료 처리 기능
document.getElementById("closeExpiredBtn")?.addEventListener("click", async () => {
    const confirmed = confirm("마감된 스터디들을 모두 종료 처리할까요?");
    if (!confirmed) return;

    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    try {
        const res = await fetch("/api/study/close-expired", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [header]: token
            }
        });

        if (res.ok) {
            alert("마감된 스터디가 종료 처리되었습니다.");
            location.reload();
        } else {
            const text = await res.text();
            alert("처리 실패: " + text);
        }
    } catch (err) {
        alert("에러 발생: " + err.message);
    }
});