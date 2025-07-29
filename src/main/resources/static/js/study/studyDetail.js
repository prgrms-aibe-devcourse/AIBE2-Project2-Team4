const urlParams = new URLSearchParams(window.location.search);
const studyId = urlParams.get('id') || window.location.pathname.split('/').pop();

const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

let participants = [];

try {
    const rawData = document.getElementById('participants-data')?.textContent?.trim();
    if (rawData) {
        participants = JSON.parse(rawData);
        if (!Array.isArray(participants)) {
            console.warn('participants는 배열이 아님:', participants);
            participants = [];
        }
    }
} catch (e) {
    console.warn('참여자 데이터 파싱 실패:', e);
    participants = [];
}

document.addEventListener('DOMContentLoaded', () => {
    console.log('Study ID:', studyId);
    updateParticipantCounts();
    setupDiscordLink();
});

//  모집 현황 표시
function updateParticipantCounts() {
    const counts = { BACKEND: 0, FRONTEND: 0, DESIGNER: 0, PLANNER: 0 };

    participants.forEach(p => {
        const jobType = p.jobType?.toUpperCase();
        if (counts.hasOwnProperty(jobType)) {
            counts[jobType]++;
        }
    });

    Object.keys(counts).forEach(type => {
        const el = document.getElementById(`${type.toLowerCase()}-current`);
        if (el) el.textContent = counts[type];
    });
}

//  스터디 신고
function reportStudy() {
    const reason = prompt('신고 사유를 입력해주세요.');
    if (!reason) return;

    fetch(`/api/report/recruitment/${studyId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify({ reason })
    }).then(res => {
        if (res.ok) {
            alert('신고가 접수되었습니다.');
        } else {
            return res.text().then(text => {
                throw new Error('신고 접수에 실패했습니다: ' + text);
            });
        }
    }).catch(err => {
        alert(err.message);
    });
}

//  스터디 신청
function applyToStudy(jobType) {
    if (!studyId || studyId === '0') {
        alert('잘못된 스터디 ID입니다.');
        return;
    }

    if (!confirm(`${jobType} 직군으로 신청하시겠습니까?`)) return;

    fetch(`/api/studies/${studyId}/apply`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify({ jobType })
    }).then(res => {
        if (res.ok) {
            alert('신청이 완료되었습니다!');
            location.reload();
        } else {
            return res.text().then(t => {
                if (t.includes('삭제된 스터디')) {
                    alert('삭제된 스터디입니다.');
                    location.href = '/study';
                    return;
                }
                throw new Error(t);
            });
        }
    }).catch(err => {
        alert('신청 실패: ' + err.message);
    });
}

//  신청 수락
function acceptApplicant(id) {
    if (!confirm('이 신청을 승인하시겠습니까?')) return;
    fetch(`/api/studies/applications/${id}/accept`, {
        method: 'POST',
        headers: { [header]: token }
    }).then(res => {
        if (res.ok) location.reload();
        else return res.text().then(t => handleStudyError(t));
    }).catch(err => {
        alert('오류: ' + err.message);
    });
}

//  신청 거절
function rejectApplicant(id) {
    if (!confirm('이 신청을 거절하시겠습니까?')) return;
    fetch(`/api/studies/applications/${id}/reject`, {
        method: 'POST',
        headers: { [header]: token }
    }).then(res => {
        if (res.ok) location.reload();
        else return res.text().then(t => handleStudyError(t));
    }).catch(err => {
        alert('오류: ' + err.message);
    });
}

//  모집 마감
function closeStudy() {
    if (!confirm('정말로 모집을 마감하시겠습니까?')) return;
    fetch(`/api/studies/${studyId}/close`, {
        method: 'POST',
        headers: { [header]: token }
    }).then(res => {
        if (res.ok) location.reload();
        else return res.text().then(t => handleStudyError(t));
    }).catch(err => {
        alert('오류: ' + err.message);
    });
}

//  Discord 링크 생성
function setupDiscordLink() {
    const url = `https://discord.gg/study${studyId}`;
    const el = document.getElementById('discord-link');
    if (el) el.href = url;
}

//  공통 에러 처리
function handleStudyError(message) {
    if (message.includes('삭제된 스터디')) {
        alert('삭제된 스터디입니다.');
        location.href = '/study';
        return;
    }
    throw new Error(message);
}
