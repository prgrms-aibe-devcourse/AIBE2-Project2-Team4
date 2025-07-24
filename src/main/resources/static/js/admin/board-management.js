document.addEventListener("DOMContentLoaded", function () {
    fetchBoardList();

    function fetchBoardList() {
        fetch("/admin/board-management/list")
            .then(res => res.json())
            .then(data => renderBoardTable(data.boards));
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
          <a href="/admin/user-management/${board.userId}" class="user-link">
            ${board.userName}
          </a>
        </td>
        <td>${board.viewCount}</td>
        <td>${board.createdAt || "-"}</td>
        <td>${board.isBlocked ? "TRUE" : "FALSE"}</td>
        <td>
            <a href="/admin/board-management/${board.id}" class="view-link">상세보기</a>
        </td>

      `;

            tbody.appendChild(tr);
        });
    }

    window.toggleBlocked = function (boardId, isBlocked) {
        fetch(`/admin/board-management/block`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ boardId, isBlocked })
        }).then(res => {
            if (!res.ok) alert("차단 상태 변경 실패");
        });
    };

    window.viewBoard = function (id) {
        window.open(`/board/${id}`, "_blank");
    };
});
