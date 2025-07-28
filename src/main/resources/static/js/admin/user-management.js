document.addEventListener("DOMContentLoaded", function () {
    const searchBtn = document.getElementById("searchBtn");
    const keywordInput = document.getElementById("searchKeyword");
    const userTableBody = document.getElementById("userTableBody");
    const pagination = document.getElementById("pagination");

    let currentPage = 1;

    function fetchUsers(page = 1) {
        const keyword = keywordInput.value;

        fetch(`/admin/user-management/list?page=${page}&keyword=${encodeURIComponent(keyword)}`)
            .then(response => {
                if (!response.ok) throw new Error("데이터 조회 실패");
                return response.json();
            })
            .then(data => {
                renderTable(data.users);
                renderPagination(data.totalPages, page);
            })
            .catch(error => {
                console.error("에러:", error);
                userTableBody.innerHTML = `<tr><td colspan="5">데이터를 불러오는 데 실패했습니다.</td></tr>`;
            });
    }

    function renderTable(users) {
        const tbody = document.getElementById('userTableBody');
        tbody.innerHTML = '';

        users.forEach((user, index) => {
            const joinDate = user.createdAt || '-';
            const status = user.isDeleted ? "탈퇴" : (user.blocked ? "차단" : "정상");

            const tr = document.createElement('tr');
            tr.innerHTML = `
      <td>${index + 1}</td>
      <td><a href="/admin/user-management/${user.id}">${user.name}</a></td>
      <td>${user.email}</td>
      <td>${joinDate}</td>
      <td>${status}</td>
    `;
            tbody.appendChild(tr);
        });
    }


    function renderPagination(totalPages, current) {
        pagination.innerHTML = "";
        for (let i = 1; i <= totalPages; i++) {
            const btn = document.createElement("button");
            btn.className = "btn btn-sm " + (i === current ? "active" : "btn-outline-secondary");
            btn.textContent = i;
            btn.addEventListener("click", () => {
                currentPage = i;
                fetchUsers(currentPage);
            });
            pagination.appendChild(btn);
        }
    }

    searchBtn.addEventListener("click", () => {
        currentPage = 1;
        fetchUsers(currentPage);
    });

    // 초기 로딩
    fetchUsers(currentPage);
});


