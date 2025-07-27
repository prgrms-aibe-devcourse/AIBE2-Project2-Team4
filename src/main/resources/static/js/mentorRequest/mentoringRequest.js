document.addEventListener('DOMContentLoaded', () => {
    loadMentors();
});

function loadMentors() {
    fetch('/api/mentoring/mentors')
        .then(res => res.json())
        .then(mentors => {
            const select = document.getElementById('mentorSelect');
            mentors.forEach(mentor => {
                const option = document.createElement('option');
                option.value = mentor.email;
                option.textContent = `${mentor.userName} (${mentor.email})`;
                select.appendChild(option);
            });
        })
        .catch(() => {
            alert('멘토 목록을 불러오는 데 실패했습니다.');
        });
}

document.getElementById('mentoring-request-form').addEventListener('submit', function (e) {
    e.preventDefault();

    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    const mentorEmail = document.getElementById('mentorSelect').value;
    const topic = document.getElementById('topic').value;
    const message = document.getElementById('message').value;

    if (!mentorEmail) {
        alert('멘토를 선택해주세요');
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
    };
    if (header && token) {
        headers[header] = token;
    }

    fetch('/api/mentoring', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            mentorEmail: mentorEmail,
            topic: topic,
            message: message
        })
    })
        .then(res => {
            if (!res.ok) throw new Error('신청 실패');
            return res.text();
        })
        .then(() => {
            alert('멘토링 신청이 완료되었습니다!');
            window.location.href = '/mentoring';
        })
        .catch(err => {
            document.getElementById('resultMessage').textContent = '신청 중 오류가 발생했습니다.';
            console.error(err);
        });
});