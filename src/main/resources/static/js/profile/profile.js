document.addEventListener('DOMContentLoaded', () => {
    // “삭제” 버튼에 대한 confirm 처리 (inline confirm 대신 JS로)
    const deleteButtons = document.querySelectorAll('.btn-outline-danger');
    deleteButtons.forEach(button => {
        button.addEventListener('click', event => {
            if (!confirm('정말 삭제하시겠습니까?')) {
                event.preventDefault();
            }
        });
    });

    // 프로필 수정 버튼(예: .btn-profile-edit)이 있으면 클릭 핸들러 추가
    const editBtn = document.querySelector('.btn-profile-edit');
    if (editBtn) {
        editBtn.addEventListener('click', () => {
            // 예시: 모달 열기, 또는 다른 로직
            console.log('프로필 수정 버튼 클릭됨');
        });
    }

    // 필요에 따라 추가 스크립트 작성…
});