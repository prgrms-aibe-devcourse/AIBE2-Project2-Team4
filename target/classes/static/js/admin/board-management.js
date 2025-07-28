document.addEventListener("DOMContentLoaded", function () {
    const searchBtn = document.getElementById("searchBtn");
    const searchKeywordInput = document.getElementById("searchKeyword");
    let currentPage = 1;

    searchBtn.addEventListener("click", () => {
        currentPage = 1;
        fetchBoardList(currentPage, searchKeywordInput.value);
    });

    function fetchBoardList(page = 1, keyword = "") {
        fetch(`/admin/board-management/list?page=${page}&keyword=${keyword}`)
            .then(res => res.json())
            .then(data => {
                renderBoardTable(data.boards);
                renderPagination(data.totalPages, page, keyword);
            });
    }

    function renderBoardTable(boards) {
        const tbody = document.getElementById("boardTableBody");
        tbody.innerHTML = "";
        boards.forEach((board, index) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
        <td>${index + 1}</td>
        <td>${board.title}</td>
        <td>
          <a href="/admin/user-management/detail/${board.userId}" class="user-link">
            ${board.userName}
          </a>
        </td>
        <td>${board.viewCount}</td>
        <td>${board.createdAt || '-'}</td>
        <td>${board.blocked ? "차단" : "정상"}</td>
        <td>
            <a href="/admin/board-management/${board.id}" class="view-link">상세보기</a>
        </td>
      `;
            tbody.appendChild(tr);
        });
    }

    function renderPagination(totalPages, currentPage, keyword) {
        const pagination = document.getElementById("pagination");
        pagination.innerHTML = "";

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement("button");
            button.textContent = i;
            button.classList.toggle("active", i === currentPage);
            button.addEventListener("click", () => {
                fetchBoardList(i, keyword);
            });
            pagination.appendChild(button);
        }
    }

    window.toggleBlocked = function (boardId, isBlocked) {
        fetch(`/admin/board-management/block`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ boardId, isBlocked })
        }).then(res => {
            if (!res.ok) alert("차단 상태 변경 실패");
        });
    };

    window.viewBoard = function (id) {
        window.open(`/admin/board-management/${id}`, "_blank");
    };

    // 첫 로딩
    fetchBoardList(currentPage);
});
