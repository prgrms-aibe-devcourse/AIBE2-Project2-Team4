// 직종별 기술스택 목록 (수정 폼용)
const techOptions = {
    "백엔드": ["Java", "Spring", "Spring Boot", "Node.js", "Express", "Python", "Django", "MySQL", "MongoDB"],
    "프론트엔드": ["HTML", "CSS", "JavaScript", "React", "Vue", "TypeScript", "Next.js"],
    "디자이너": ["Figma", "Photoshop", "Illustrator", "Sketch", "Adobe XD"],
    "기획자": ["Notion", "Jira", "Confluence", "Trello", "Excel"]
};

// URL에서 게시글 ID 추출
const boardId = window.location.pathname.split("/").pop();

$(document).ready(function () {
    // CSRF 토큰 설정 (Ajax 요청 시 자동 헤더 첨부)
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    // 게시글 상세 데이터 GET 요청, 폼에 초기값 세팅
    $.ajax({
        url: `/api/board/${boardId}`,
        type: 'GET',
        success: function (data) {
            $('#title').val(data.title);
            $('#content').val(data.content);
            $('#jobType').val(data.jobType);
            // 기술스택 드롭다운 업데이트 (선택값 포함)
            updateTechStack(data.jobType, data.techStack);
        }
    });

    // 직종 변경 시 기술스택 옵션 업데이트
    $('#jobType').on('change', function () {
        updateTechStack(this.value, null);
    });

    // 수정 폼 제출 처리
    $('#editForm').on('submit', function (e) {
        e.preventDefault();

        const payload = {
            title: $('#title').val(),
            content: $('#content').val(),
            jobType: $('#jobType').val(),
            techStack: $('#techStack').val()
        };

        // PUT 요청으로 수정 내용 서버에 전송
        $.ajax({
            url: `/api/board/${boardId}`,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function () {
                alert('수정이 완료되었습니다.');
                // 수정 성공 후 상세 페이지로 이동
                location.href = `/board/${boardId}`;
            },
            error: function () {
                alert('글 작성자만 수정 가능합니다.');
            }
        });
    });
});

// 기술스택 드롭다운 업데이트 함수
// selectedJob: 선택된 직종, selectedTech: 선택된 기술스택 (있으면 선택 상태로)
function updateTechStack(selectedJob, selectedTech) {
    const techSelect = $('#techStack');
    techSelect.empty();

    if (techOptions[selectedJob]) {
        techOptions[selectedJob].forEach(stack => {
            const option = $('<option>').val(stack).text(stack);
            if (stack === selectedTech) option.attr('selected', true);
            techSelect.append(option);
        });
    } else {
        // 직종 미선택 시 안내 옵션 추가
        techSelect.append('<option selected disabled>직종을 먼저 선택해 주세요</option>');
    }
}