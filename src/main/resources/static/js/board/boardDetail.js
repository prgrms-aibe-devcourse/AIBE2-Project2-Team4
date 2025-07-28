const boardId = window.location.pathname.split('/').pop();

$(document).ready(function () {
    const token = $("meta[name='_csrf']").attr('content');
    const header = $("meta[name='_csrf_header']").attr('content');
    $(document).ajaxSend(function (e, xhr) {
        xhr.setRequestHeader(header, token);
    });

    // 게시글 불러오기
    $.get(`/api/board/${boardId}`)
        .done(function (data) {
            $('#boardTitle').text(data.title);
            $('#boardMeta').contents().first()[0].textContent = `작성자: ${data.userName} | 작성일: ${data.createdAt.replace('T', ' ').slice(0, 16)} | 조회수: ${data.viewCount} `;
            $('#editBtn').attr('href', `/board/edit/${data.id}`);
            $('#boardContent').text(data.content);
        })
        .fail(function(xhr) {
            if (xhr.status === 400) {
                alert("삭제된 게시물입니다.");
                location.href = "/board";
            } else {
                alert("게시물을 불러올 수 없습니다.");
                location.href = "/board";
            }
        });

    // 게시글 삭제
    $('#deleteBtn').click(function () {
        if (confirm('삭제하시겠습니까?')) {
            $.ajax({
                url: `/api/board/${boardId}`,
                type: 'DELETE'
            })
                .done(() => (location.href = '/board'))
                .fail(() => alert('글 작성자만 삭제 가능합니다.'));
        }
    });

    // 게시글 신고
    $('#reportBoardBtn').click(function () {
        const reason = prompt('신고 사유를 입력해주세요.');
        if (!reason) return;
        $.ajax({
            url: `/board/report/${boardId}`,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ reason }),
            success: () => alert('신고가 접수되었습니다.'),
            error: () => alert('신고 접수에 실패했습니다.')
        });
    });

    // 댓글 작성
    $('#commentForm').submit(function (e) {
        e.preventDefault();
        const content = $('#commentContent').val();
        const parentId = $('#parentId').val() || null;
        $.ajax({
            url: `/api/board/${boardId}/comments`,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ content, parentId }),
            success: () => {
                $('#commentContent').val('');
                $('#parentId').val('');
                loadComments();
            },
            error: () => alert('댓글 등록 실패')
        });
    });

    // 댓글 목록 불러오기
    function loadComments() {
        $.get(`/api/board/${boardId}/comments`, function (comments) {
            const list = $('#commentList').empty();
            if (comments.length === 0) {
                return list.append('<p class="text-muted">댓글이 없습니다.</p>');
            }
            comments.forEach(c => renderComment(c, list));
        });
    }

    // 댓글 렌더링 (대댓글 포함 재귀)
    function renderComment(c, container, depth = 0) {
        // 차단된 댓글 처리
        if (c.isBlocked) {
            const div = $(`
              <div class="border rounded p-2 mb-2 ${depth > 0 ? 'reply' : ''}" data-id="${c.id}">
                <div class="text-muted">삭제된 댓글입니다.</div>
              </div>
            `);
            container.append(div);

            // 차단된 댓글의 자식 댓글들은 여전히 표시
            if (c.children && c.children.length) {
                c.children.forEach(child => renderComment(child, container, depth + 1));
            }
            return;
        }

        const div = $(`
          <div class="border rounded p-2 mb-2 ${depth > 0 ? 'reply' : ''}" data-id="${c.id}">
            <div>
              <strong>${c.userName}</strong>
              <button class="btn-report report-btn" title="댓글 신고" data-id="${c.id}" data-type="COMMENT">신고하기</button>
            </div>
            <div class="comment-content">${c.content}</div>
            <div class="text-muted small">${c.createdAt.replace('T', ' ').slice(0, 16)}</div>
            ${c.editable ? `
              <button class="btn btn-sm btn-outline-secondary edit-btn mt-1">수정</button>
              <button class="btn btn-sm btn-outline-danger delete-btn mt-1">삭제</button>
            ` : ''}
            <button class="btn btn-sm btn-outline-primary reply-btn mt-1">답글</button>
          </div>
        `);

        container.append(div);

        if (c.children && c.children.length) {
            c.children.forEach(child => renderComment(child, container, depth + 1));
        }
    }

    // 댓글 수정 버튼 클릭
    $(document).on('click', '.edit-btn', function () {
        const div = $(this).closest('[data-id]');
        const id = div.data('id');
        const contentDiv = div.find('.comment-content');
        const original = contentDiv.text();
        const textarea = $(`<textarea class="form-control mb-1" rows="2">${original}</textarea>`);
        const saveBtn = $('<button class="btn btn-sm btn-primary me-1">저장</button>');
        const cancelBtn = $('<button class="btn btn-sm btn-secondary">취소</button>');
        contentDiv.replaceWith(textarea);
        $(this).after(cancelBtn).after(saveBtn).hide();

        saveBtn.click(() => {
            $.ajax({
                url: `/api/board/${boardId}/comments/${id}`,
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify({ content: textarea.val() }),
                success: loadComments,
                error: () => alert('글 작성자만 수정 가능합니다.')
            });
        });

        cancelBtn.click(() => {
            textarea.replaceWith(`<div class="comment-content">${original}</div>`);
            saveBtn.remove();
            cancelBtn.remove();
            div.find('.edit-btn').show();
        });
    });

    // 댓글 삭제 버튼 클릭
    $(document).on('click', '.delete-btn', function () {
        const id = $(this).closest('[data-id]').data('id');
        if (confirm('댓글 삭제하시겠습니까?')) {
            $.ajax({
                url: `/api/board/${boardId}/comments/${id}`,
                type: 'DELETE',
                success: loadComments
            });
        }
    });

    // 대댓글 작성 버튼 클릭
    $(document).on('click', '.reply-btn', function () {
        const id = $(this).closest('[data-id]').data('id');
        $('#parentId').val(id);
        $('#commentContent').focus();
    });

    // 댓글 신고 버튼 클릭
    $(document).on('click', '.report-btn', function () {
        const targetId = $(this).data('id');
        const targetType = $(this).data('type');
        const reason = prompt('신고 사유를 입력해주세요.');
        if (!reason) return;

        const url =
            targetType === 'COMMENT'
                ? `/api/report/comment/${targetId}`
                : `/api/report/board/${targetId}`;

        $.ajax({
            url: url,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ reason }),
            success: () => alert('신고가 접수되었습니다.'),
            error: () => alert('신고 접수에 실패했습니다.')
        });
    });

    loadComments();
});