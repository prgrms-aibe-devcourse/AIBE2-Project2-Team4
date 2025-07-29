document.addEventListener('DOMContentLoaded', () => {
    // 1) 프로젝트 삭제 확인 다이얼로그
    document.querySelectorAll('form[action*="/projects/delete"]').forEach(form => {
        form.addEventListener('submit', e => {
            const confirmed = window.confirm('정말 이 프로젝트를 삭제하시겠습니까?');
            if (!confirmed) {
                e.preventDefault();
            }
        });
    });

    // 2) 프로젝트 참여 버튼 비동기 처리 예시
    document.querySelectorAll('form[action*="/projects/join"]').forEach(form => {
        form.addEventListener('submit', e => {
            e.preventDefault();
            const btn = form.querySelector('button');
            btn.disabled = true;
            btn.innerText = '참여 중…';

            fetch(form.action, {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': form.querySelector('input[name="_csrf"]').value,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams(new FormData(form))
            })
                .then(resp => {
                    if (resp.ok) {
                        btn.innerText = '참여 완료';
                        btn.classList.remove('btn-outline-success');
                        btn.classList.add('btn-success');
                    } else {
                        return Promise.reject('참여에 실패했습니다.');
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert(err);
                    btn.disabled = false;
                    btn.innerText = '참여하기';
                });
        });
    });

    // 3) 프로젝트 검색 폼 (엔터 키 제출)
    const searchInput = document.querySelector('form.project-search input[name="q"]');
    if (searchInput) {
        searchInput.addEventListener('keypress', e => {
            if (e.key === 'Enter') {
                e.target.closest('form').submit();
            }
        });
    }

    // 4) 카드 호버 시 그림자 제어 (추가 인터랙션)
    document.querySelectorAll('.card-hover').forEach(card => {
        card.addEventListener('mouseover', () => {
            card.style.boxShadow = '0 6px 18px rgba(0,0,0,0.15)';
        });
        card.addEventListener('mouseout', () => {
            card.style.boxShadow = '';
        });
    });
});