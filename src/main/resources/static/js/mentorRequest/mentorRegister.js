const csrfToken = $('meta[name="_csrf"]').attr('content');
const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

$.ajaxSetup({
    beforeSend: function (xhr) {
        xhr.setRequestHeader(csrfHeader, csrfToken);
    }
});

$('#mentorRegisterForm').submit(function (e) {
    e.preventDefault();

    $.ajax({
        url: '/api/mentoring/register',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            techStack: this.techStack.value,
            description: this.description.value
        }),
        success: function () {
            alert('멘토로 등록되었습니다.');
            location.href = '/mentoring';
        },
        error: function () {
            alert('등록에 실패했습니다.');
        }
    });
});