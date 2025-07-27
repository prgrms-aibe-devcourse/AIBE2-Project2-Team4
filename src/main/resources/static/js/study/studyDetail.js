const studyId = /*[[${study.id}]]*/ 0;
const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
const participants = /*[[${participantsJson}]]*/ [];

document.addEventListener('DOMContentLoaded', () => updateParticipantCounts());

function updateParticipantCounts() {
    const counts = { BACKEND: 0, FRONTEND: 0, DESIGNER: 0, PLANNER: 0 };
    participants.forEach(p => {
        const jobType = p.jobType?.toUpperCase();
        if (counts[jobType] !== undefined) counts[jobType]++;
    });
    ['backend', 'frontend', 'designer', 'planner'].forEach(type => {
        const el = document.getElementById(`${type}-current`);
        if (el) el.textContent = counts[type.toUpperCase()];
    });
}

function applyToStudy(jobType) {
    if (!confirm(`${jobType} 직군으로 신청하시겠습니까?`)) return;
    fetch(`/api/studies/${studyId}/apply`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', [header]: token },
        body: JSON.stringify({ jobType })
    }).then(res => res.ok ? location.reload() : res.text().then(t => { throw new Error(t); }))
        .catch(err => alert('신청 실패: ' + err.message));
}

function acceptApplicant(id) {
    if (!confirm('이 신청을 승인하시겠습니까?')) return;
    fetch(`/api/studies/applications/${id}/accept`, {
        method: 'POST',
        headers: { [header]: token }
    }).then(res => res.ok ? location.reload() : res.text().then(t => { throw new Error(t); }))
        .catch(err => alert('오류: ' + err.message));
}

function rejectApplicant(id) {
    if (!confirm('이 신청을 거절하시겠습니까?')) return;
    fetch(`/api/studies/applications/${id}/reject`, {
        method: 'POST',
        headers: { [header]: token }
    }).then(res => res.ok ? location.reload() : res.text().then(t => { throw new Error(t); }))
        .catch(err => alert('오류: ' + err.message));
}

function closeStudy() {
    if (!confirm('정말로 모집을 마감하시겠습니까?')) return;
    fetch(`/api/studies/${studyId}/close`, {
        method: 'POST',
        headers: { [header]: token }
    }).then(res => res.ok ? location.reload() : res.text().then(t => { throw new Error(t); }))
        .catch(err => alert('오류: ' + err.message));
}

function generateDiscordLink() {
    const url = `https://discord.gg/study${studyId}`;
    const el = document.getElementById('discord-link');
    if (el) el.href = url;
    navigator.clipboard.writeText(url).then(() => alert('Discord 링크가 복사되었습니다!\n' + url));
}