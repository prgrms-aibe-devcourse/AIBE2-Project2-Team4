document.addEventListener('DOMContentLoaded', () => {
    loadMentors();
});

function loadMentors() {
    fetch('/api/mentoring/mentors')
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('mentorSelect');

            // 새로운 데이터 구조 처리
            let mentors = data;
            if (data.mentors) {
                mentors = data.mentors;  // {mentors: [...], currentUserEmail: "..."} 형태
            }

            console.log('멘토 목록:', mentors);

            mentors.forEach(mentor => {
                const option = document.createElement('option');
                option.value = mentor.email;
                option.textContent = `${mentor.userName} (${mentor.email})`;
                select.appendChild(option);
            });
        })
        .catch((error) => {
            console.error('멘토 목록 로딩 실패:', error);
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

    console.log('신청 데이터:', { mentorEmail, topic, message });

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

    console.log('요청 헤더:', headers);

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
            console.log('응답 상태:', res.status);
            if (!res.ok) {
                return res.text().then(text => {
                    throw new Error(`신청 실패 (${res.status}): ${text}`);
                });
            }
            return res.text();
        })
        .then(() => {
            alert('멘토링 신청이 완료되었습니다!');
            window.location.href = '/mentoring';
        })
        .catch(err => {
            console.error('신청 에러:', err);
            document.getElementById('resultMessage').textContent = '신청 중 오류가 발생했습니다: ' + err.message;
            alert('신청 실패: ' + err.message);
        });
});