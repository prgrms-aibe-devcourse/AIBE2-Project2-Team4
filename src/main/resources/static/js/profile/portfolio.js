document.addEventListener('DOMContentLoaded', () => {
    // 1) 포트폴리오 등록/수정 폼: 파일 입력 필드 추가 버튼
    const addFileBtn = document.getElementById('add-file-btn');
    const fileInputsContainer = document.getElementById('file-inputs');
    if (addFileBtn && fileInputsContainer) {
        addFileBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const input = document.createElement('input');
            input.type = 'file';
            input.name = 'files';
            input.accept = 'image/*';
            input.className = 'form-control mb-2';
            fileInputsContainer.appendChild(input);
        });
    }

    // 2) 포트폴리오 상세 페이지: ‘공유하기’ 버튼 → URL 복사
    const shareBtn = document.getElementById('share-btn');
    if (shareBtn) {
        shareBtn.addEventListener('click', () => {
            navigator.clipboard.writeText(window.location.href)
                .then(() => {
                    const original = shareBtn.textContent;
                    shareBtn.textContent = '복사됨!';
                    setTimeout(() => { shareBtn.textContent = original; }, 2000);
                })
                .catch(err => console.error('클립보드 복사 실패:', err));
        });
    }

    // 3) 포트폴리오 삭제 버튼: 확인 후 폼 제출
    const deleteForms = document.querySelectorAll('.btn-portfolio-delete');
    deleteForms.forEach(btn => {
        btn.addEventListener('click', (e) => {
            if (!confirm('정말 이 포트폴리오를 삭제하시겠습니까?')) {
                e.preventDefault();
            }
        });
    });
});