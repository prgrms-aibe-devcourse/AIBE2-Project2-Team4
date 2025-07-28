// 직종별 기술스택 목록 정의
const techStackMap = {
    BACKEND: ['Java', 'Spring', 'Node.js', 'Express', 'MySQL', 'MongoDB', 'Redis'],
    FRONTEND: ['React', 'Vue.js', 'HTML/CSS', 'JavaScript', 'TypeScript', 'Next.js'],
    DESIGNER: ['Figma', 'Photoshop', 'Illustrator', 'Adobe XD', 'Sketch'],
    PLANNER: ['Notion', 'Jira', 'Slack', 'Confluence', 'Trello']
};

// 페이지 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {

    // HTML 요소들 찾기
    const stackContainer = document.getElementById('stackContainer');

    // 기술스택 선택 영역과 직종 정보 정의
    const roles = [
        { key: 'BACKEND', inputId: 'backendInput' },
        { key: 'FRONTEND', inputId: 'frontendInput' },
        { key: 'DESIGNER', inputId: 'designerInput' },
        { key: 'PLANNER', inputId: 'plannerInput' }
    ];

    if (!stackContainer) {
        console.error('stackContainer not found! HTML에 <div id="stackContainer"></div>가 있는지 확인하세요.');
        return;
    }

    roles.forEach(role => {
        const element = document.getElementById(role.inputId);
        console.log(`${role.key} input (${role.inputId}):`, element);
        if (!element) {
            console.error(`Missing element: ${role.inputId}`);
        }
    });

    // 각 직종별 인원수 입력 필드에 이벤트 리스너 등록
    roles.forEach(role => {
        const inputElement = document.getElementById(role.inputId);
        if (inputElement) {
            inputElement.addEventListener('input', function () {
                console.log(`${role.key} 인원수 변경: ${this.value}`);
                const count = parseInt(this.value) || 0;
                updateStackSelection(role.key, count, stackContainer);
            });
        }
    });

    // 직종별 인원수에 따라 기술스택 선택 UI를 동적으로 생성/제거
    function updateStackSelection(roleKey, count, container) {
        console.log(`updateStackSelection: ${roleKey}, count: ${count}`);

        const sectionId = `stack-${roleKey}`;
        const existing = document.getElementById(sectionId);

        // 기존 섹션이 있으면 제거
        if (existing) {
            existing.remove();
            console.log(`🗑기존 섹션 제거: ${sectionId}`);
        }

        // 인원수가 0보다 크면 기술스택 선택 UI 생성
        if (count > 0) {
            console.log(`새 섹션 생성: ${sectionId}`);

            const div = document.createElement('div');
            div.id = sectionId;
            div.classList.add('mb-3', 'tech-stack-section');

            // 기술스택 선택 UI HTML 구성
            div.innerHTML = `
                <label class="form-label">${getRoleDisplayName(roleKey)} 기술스택 (${count}명)</label>
                <select id="select-${roleKey}" class="form-select mt-1">
                    <option value="">기술스택 선택</option>
                    ${techStackMap[roleKey].map(stack => `<option value="${stack}">${stack}</option>`).join('')}
                </select>
                <div class="tag-container mt-2" id="tags-${roleKey}"></div>
            `;

            container.appendChild(div);
            console.log(`섹션 추가 완료: ${sectionId}`);

            // 기술스택 선택 드롭다운에 이벤트 리스너 추가
            const select = div.querySelector(`#select-${roleKey}`);
            if (select) {
                select.addEventListener('change', () => {
                    const value = select.value;
                    if (value && value.trim() !== '') {
                        console.log(`태그 추가: ${value}`);
                        addTag(roleKey, value);
                        select.value = '';
                    }
                });
            }
        }
    }

    // 직종 키를 한글 표시명으로 변환
    function getRoleDisplayName(roleKey) {
        const displayNames = {
            'BACKEND': '백엔드',
            'FRONTEND': '프론트엔드',
            'DESIGNER': '디자이너',
            'PLANNER': '기획자'
        };
        return displayNames[roleKey] || roleKey;
    }

    // 선택된 기술스택을 태그로 추가
    function addTag(roleKey, value) {
        const tagsDiv = document.getElementById(`tags-${roleKey}`);
        if (!tagsDiv) {
            console.error(`Tags container not found: tags-${roleKey}`);
            return;
        }

        // 이미 동일한 태그가 있는지 확인 (중복 방지)
        const existingTags = [...tagsDiv.children];
        if (existingTags.some(tag => tag.dataset.value === value)) {
            console.log(`Tag already exists: ${value}`);
            return;
        }

        // 새 태그 요소 생성
        const tag = document.createElement('div');
        tag.classList.add('tag', 'badge', 'bg-primary', 'me-1', 'mb-1', 'd-inline-flex', 'align-items-center');
        tag.dataset.value = value;
        tag.innerHTML = `
            <span class="me-1">${value}</span>
            <span class="remove-tag" style="cursor: pointer; font-weight: bold;">&times;</span>
            <input type="hidden" name="techStacks_${roleKey}" value="${value}">
        `;

        // 태그 제거 버튼에 이벤트 리스너 추가
        const removeBtn = tag.querySelector('.remove-tag');
        if (removeBtn) {
            removeBtn.onclick = (e) => {
                e.preventDefault();
                tag.remove();
                console.log(`태그 제거: ${value}`);
            };
        }

        // 태그 컨테이너에 추가
        tagsDiv.appendChild(tag);
        console.log(`태그 추가 완료: ${value} for ${roleKey}`);
    }
});