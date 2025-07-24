document.addEventListener("DOMContentLoaded", function () {
    function fetchUnresolvedReportCount() {
        fetch("/admin/api/dashboard/unresolved-report-count")
            .then(res => res.json())
            .then(count => {
                document.getElementById("unresolvedReportCount").textContent = `${count} 건`;
            });
    }


    fetchUnresolvedReportCount();


    const newUserCtx = document.getElementById('newUserChart').getContext('2d');
    const leftUserCtx = document.getElementById('leftUserChart').getContext('2d');
    const matchingCtx = document.getElementById('matchingChart').getContext('2d');

    let newUserChart, leftUserChart, matchingChart;

    function drawChart(ctx, data, label, color, backgroundColor, chartRef) {
        if (chartRef.chart) {
            chartRef.chart.destroy();
        }

        chartRef.chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.map(d => d.date),
                datasets: [{
                    label: label,
                    data: data.map(d => d.count),
                    borderColor: color,
                    backgroundColor: backgroundColor,
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: true },
                    tooltip: { mode: 'index', intersect: false }
                },
                scales: {
                    x: { title: { display: true, text: '날짜' }},
                    y: {
                        beginAtZero: true,
                        title: { display: true, text: label },
                        ticks: { precision: 0 }
                    }
                }
            }
        });
    }

    function drawMatchingChart(data) {
        if (matchingChart) matchingChart.destroy();

        matchingChart = new Chart(matchingCtx, {
            type: 'bar',
            data: {
                labels: data.map(item => item.status),
                datasets: [{
                    label: "매칭 수",
                    data: data.map(item => item.count),
                    backgroundColor: '#4e73df'
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            precision: 0
                        }
                    }
                }
            }
        });
    }

    function fetchAndRender(start, end) {
        const newUserRef = { chart: newUserChart, set chart(val) { newUserChart = val; }, get chart() { return newUserChart; } };
        const leftUserRef = { chart: leftUserChart, set chart(val) { leftUserChart = val; }, get chart() { return leftUserChart; } };

        fetch(`/admin/api/dashboard/signup-stats?start=${start}&end=${end}`)
            .then(res => res.json())
            .then(data => drawChart(newUserCtx, data, '일별 신규 가입자 수', '#4e73df', 'rgba(78, 115, 223, 0.1)', newUserRef));

        fetch(`/admin/api/dashboard/withdraw-stats?start=${start}&end=${end}`)
            .then(res => res.json())
            .then(data => drawChart(leftUserCtx, data, '일별 탈퇴자 수', '#e74a3b', 'rgba(231, 74, 59, 0.1)', leftUserRef));
    }

    function fetchAndRenderMatchingStatus() {
        fetch("/admin/api/dashboard/matching-status")
            .then(res => res.json())
            .then(data => drawMatchingChart(data));
    }

    // 초기 설정 (최근 7일)
    const today = new Date();
    const start = new Date();
    start.setDate(today.getDate() - 6);

    const formatDate = (d) => d.toISOString().split('T')[0];
    document.getElementById('startDate').value = formatDate(start);
    document.getElementById('endDate').value = formatDate(today);

    fetchAndRender(formatDate(start), formatDate(today));
    fetchAndRenderMatchingStatus(); // 매칭 현황 차트도 같이 로드

    // 조회 버튼 이벤트
    document.getElementById('loadStatsBtn').addEventListener('click', () => {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        if (startDate && endDate) {
            fetchAndRender(startDate, endDate);
        }
    });
});
