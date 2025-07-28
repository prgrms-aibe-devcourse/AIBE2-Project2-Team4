document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('#studyTabs .nav-link').forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            document.querySelectorAll('#studyTabs .nav-link').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            loadTabContent(tab.dataset.type);
        });
    });
    loadTabContent('recruiting');
});

const statusBadgeMap = {
    true: '<span class="badge bg-secondary">모집마감</span>',
    false: '<span class="badge bg-success">모집중</span>'
};

function loadTabContent(type) {
    const container = document.getElementById('study-content');
    container.innerHTML = '<p>로딩 중...</p>';

    let url = '/api/studies/tab-studies?tab=' + type;

    fetch(url)
        .then(res => res.json())
        .then(data => {
            container.innerHTML = '';

            if (!data || data.length === 0) {
                container.innerHTML = '<p>표시할 데이터가 없습니다.</p>';
                return;
            }

            data.forEach(study => {
                const card = document.createElement('a');
                card.className = 'card mb-3 p-3 clickable-card';
                card.href = `/study/${study.id}`;
                card.style.display = 'block';

                const recruitInfo = [];
                if (study.backendRecruit > 0) recruitInfo.push(`백엔드 ${study.backendRecruit}명`);
                if (study.frontendRecruit > 0) recruitInfo.push(`프론트엔드 ${study.frontendRecruit}명`);
                if (study.designerRecruit > 0) recruitInfo.push(`디자이너 ${study.designerRecruit}명`);
                if (study.plannerRecruit > 0) recruitInfo.push(`기획자 ${study.plannerRecruit}명`);

                let techStackHtml = '';
                if (study.techStacks && study.techStacks.length > 0) {
                    const techList = study.techStacks.join(', ');
                    techStackHtml = `<p class="card-text text-muted">기술스택: ${techList}</p>`;
                }

                const userName = study.user ? study.user.name : '알 수 없음';

                let badgeHtml = '';
                if (study.completed) {
                    badgeHtml = '<span class="badge bg-dark">종료됨</span>';
                } else {
                    badgeHtml = statusBadgeMap[study.closed] || '';
                }

                card.innerHTML = `
        <div class="card-body">
            <h5 class="card-title">${study.title}</h5>
            <p class="card-text">${study.content}</p>
            <p class="card-text text-muted">작성자: ${userName}</p>
            <p class="card-text text-muted">마감일: ${new Date(study.deadline).toLocaleDateString()}</p>
            <p class="card-text text-muted">모집: ${recruitInfo.join(', ')}</p>
            ${techStackHtml}
            ${badgeHtml}

            <button class="btn btn-sm btn-danger mt-2" onclick="event.preventDefault(); deleteStudy(${study.id});">삭제 (테스트용)</button>
            <button class="btn btn-sm btn-dark mt-2 ms-2" onclick="event.preventDefault(); completeStudy(${study.id});">스터디 완료처리 (테스트용)</button>
        </div>
    `;
                container.appendChild(card);
            });
        })
        .catch(err => {
            console.error('Error:', err);
            container.innerHTML = `<p class="text-danger">오류 발생: ${err.message}</p>`;
        });
}

function deleteStudy(studyId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    fetch(`/api/studies/${studyId}`, {
        method: 'DELETE',
        headers: {
            [header]: token
        }
    })
        .then(res => {
            if (res.ok) {
                alert('삭제되었습니다.');
                loadTabContent(document.querySelector('#studyTabs .nav-link.active').dataset.type);
            } else {
                return res.text().then(t => { throw new Error(t); });
            }
        })
        .catch(err => alert('삭제 실패: ' + err.message));
}

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