document.addEventListener("DOMContentLoaded", function () {
    const searchBtn = document.getElementById("searchBtn");
    const searchInput = document.getElementById("searchKeyword");
    let currentPage = 1;

    searchBtn.addEventListener("click", () => {
        currentPage = 1;
        fetchReportList(currentPage, searchInput.value);
    });

    function fetchReportList(page = 1, keyword = "") {
        fetch(`/admin/report-management/list?page=${page}&keyword=${keyword}`)
            .then(res => res.json())
            .then(data => {
                renderReportTable(data.reports);
                renderPagination(data.totalPages, page, keyword);
            });
    }

    function renderReportTable(reports) {
        const tbody = document.getElementById("reportTableBody");
        tbody.innerHTML = "";

        reports.forEach((report, index) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
        <td>${index + 1}</td>
        <td>${report.reporterName}</td>
        <td>${report.targetSummary}</td>
        <td>${report.reason}</td>
        <td>${report.createdAt || '-'}</td>
        <td>${report.status}</td>
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
                fetchReportList(i, keyword);
            });
            pagination.appendChild(button);
        }
    }

    fetchReportList(currentPage);
});
