// 직종별 기술스택 목록 (글쓰기 & 필터용)
const techOptions = {
    "백엔드": ["Java", "Spring", "MySQL", "Spring Boot", "Node.js", "Express", "MongoDB", "JPA", "Redis", "Kafka"],
    "프론트엔드": ["HTML", "CSS", "JavaScript", "React", "Vue", "Next.js", "TypeScript", "Tailwind"],
    "디자이너": ["Figma", "Adobe XD", "Photoshop", "Illustrator"],
    "기획자": ["Notion", "Jira", "Confluence", "Google Docs"]
};

// 직종 선택 시 기술스택 드롭다운 동기화
function updateTechStack(jobSelectId, techSelectId) {
    const job = document.getElementById(jobSelectId).value;
    const techSelect = document.getElementById(techSelectId);
    techSelect.innerHTML = '<option value="" selected disabled>기술스택</option>';
    (techOptions[job] || []).forEach(stack => {
        const opt = document.createElement('option');
        opt.value = stack;
        opt.textContent = stack;
        techSelect.appendChild(opt);
    });
}

// URL 쿼리 파라미터 가져오기
function getQueryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}

// URL 쿼리 파라미터 설정
function setQueryParams(params) {
    const url = new URL(window.location.href);
    Object.entries(params).forEach(([k, v]) => {
        if (v) url.searchParams.set(k, v);
        else url.searchParams.delete(k);
    });
    history.pushState({}, '', url);
}

// 게시글 목록 로드 (Ajax)
function loadBoards(page = 0) {
    const jobType = $('#jobTypeFilter').val();
    const techStack = $('#techStackFilter').val();
    const keyword = $('#keywordInput').val();
    setQueryParams({ page, jobType, techStack, keyword });

    $.get('/api/board', { page, size: 10, jobType, techStack, keyword })
        .done(res => {
            renderBoardList(res);
            renderPagination(res);
        })
        .fail(() => alert('게시글 로딩 실패'));
}

// 내 글 보기 목록 로드 (Ajax)
function loadMyBoards(page = 0) {
    $.get('/api/board/my', { page, size: 10 })
        .done(res => {
            renderBoardList(res);
            renderPagination(res);
        })
        .fail(() => alert('내 글 목록 로딩 실패'));
}

// 게시글 목록 렌더링
function renderBoardList(res) {
    const tbody = $('#boardTableBody').empty();
    if (res.content.length === 0) {
        return tbody.append('<tr><td colspan="7">게시글 없음</td></tr>');
    }
    res.content.forEach(b => {
        const created = b.createdAt.replace('T', ' ').substring(0, 16);
        const row = `
        <tr data-id="${b.id}">
          <td>${b.id}</td>
          <td>${b.userName}</td>
          <td>${b.title}</td>
          <td>${b.jobType ?? '-'}</td>
          <td>${b.techStack ?? '-'}</td>
          <td>${b.viewCount}</td>
          <td>${created}</td>
        </tr>
      `;
        tbody.append(row);
    });

    // 게시글 클릭 시 상세 페이지 이동
    $('tr[data-id]').click(function () {
        const id = $(this).data('id');
        location.href = `/board/${id}`;
    });
}

// 페이지네이션 렌더링
function renderPagination(res) {
    const container = $('#pagination').empty();
    if (res.totalPages <= 1) return;

    const appendBtn = (n, label, isActive, isDisabled) => {
        const activeClass = isActive ? 'btn-primary' : 'btn-outline-primary';
        const disabledAttr = isDisabled ? 'disabled' : '';
        container.append(`
        <button class="btn btn-sm mx-1 ${activeClass}" ${disabledAttr} onclick="loadBoards(${n})">${label}</button>
      `);
    };

    appendBtn(res.number - 1, '«', false, res.first);
    for (let i = 0; i < res.totalPages; i++) {
        appendBtn(i, i + 1, res.number === i, false);
    }
    appendBtn(res.number + 1, '»', false, res.last);
}

// 글쓰기 폼 열기
function openWriteForm() {
    $('#overlay').show();
    $('#writeForm').show();
}

// 글쓰기 폼 닫기
function closeWriteForm() {
    $('#overlay').hide();
    $('#writeForm').hide();
}

// 초기 실행 및 이벤트 바인딩
$(function () {
    // CSRF 토큰 설정
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    // URL 파라미터 기반 필터 초기값 반영
    const jobType = getQueryParam("jobType");
    const techStack = getQueryParam("techStack");
    const keyword = getQueryParam("keyword");
    const page = Number(getQueryParam("page") || 0);

    if (jobType) $('#jobTypeFilter').val(jobType);
    if (techStack) {
        updateTechStack('jobTypeFilter', 'techStackFilter');
        $('#techStackFilter').val(techStack);
    }
    if (keyword) $('#keywordInput').val(keyword);

    loadBoards(page);

    // 필터 변경 시 목록 갱신
    $('#jobTypeFilter').on('change', function () {
        updateTechStack('jobTypeFilter', 'techStackFilter');
        loadBoards(0);
    });
    $('#techStackFilter').on('change', () => loadBoards(0));
    $('#searchButton').on('click', () => loadBoards(0));

    // 글쓰기 폼 직종 변경 시 기술스택 드롭다운 변경
    $('#jobTypeForm').on('change', function () {
        updateTechStack('jobTypeForm', 'techStackForm');
    });

    // 글 등록 처리 Ajax
    $(document).on('submit', '#createForm', function (e) {
        e.preventDefault();
        const formData = {
            title: this.title.value,
            content: this.content.value,
            jobType: this.jobType.value,
            techStack: this.techStack.value
        };
        $.ajax({
            url: '/api/board',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function () {
                alert('게시글이 등록되었습니다.');
                closeWriteForm();

                // 필터 초기화 후 목록 새로고침
                $('#jobTypeFilter').val('');
                $('#techStackFilter').empty().append('<option value="" selected disabled>기술스택</option>');
                $('#keywordInput').val('');

                loadBoards(0);
            },
            error: function () {
                alert('게시글 등록 실패');
            }
        });
    });

    // 내 글 보기 버튼 이벤트 바인딩
    $('#myBoardsBtn').on('click', function () {
        loadMyBoards(0);
    });
});