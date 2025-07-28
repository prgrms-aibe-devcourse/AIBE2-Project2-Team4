const techStackMap = {
    BACKEND: ['Java', 'Spring', 'Node.js', 'Express'],
    FRONTEND: ['React', 'Vue.js', 'HTML/CSS', 'JavaScript'],
    DESIGNER: ['Figma', 'Photoshop', 'Illustrator'],
    PLANNER: ['Notion', 'Jira', 'Slack']
};

const stackContainer = document.getElementById('stackContainer');
const roles = [
    { key: 'BACKEND', inputId: 'backendInput' },
    { key: 'FRONTEND', inputId: 'frontendInput' },
    { key: 'DESIGNER', inputId: 'designerInput' },
    { key: 'PLANNER', inputId: 'plannerInput' }
];

roles.forEach(role => {
    document.getElementById(role.inputId).addEventListener('input', function () {
        updateStackSelection(role.key, parseInt(this.value));
    });
});

function updateStackSelection(roleKey, count) {
    const sectionId = `stack-${roleKey}`;
    const existing = document.getElementById(sectionId);
    if (existing) existing.remove();

    if (count > 0) {
        const div = document.createElement('div');
        div.id = sectionId;
        div.classList.add('mb-3');
        div.innerHTML = `
                <label>${roleKey} 기술 스택</label>
                <select id="select-${roleKey}" class="form-control mt-1">
                    <option value="">선택</option>
                    ${techStackMap[roleKey].map(s => `<option value="${s}">${s}</option>`).join('')}
                </select>
                <div class="tag-container" id="tags-${roleKey}"></div>
            `;
        stackContainer.appendChild(div);

        const select = div.querySelector(`#select-${roleKey}`);
        select.addEventListener('change', () => {
            const value = select.value;
            if (value) {
                addTag(roleKey, value);
                select.value = '';
            }
        });
    }
}

function addTag(roleKey, value) {
    const tagsDiv = document.getElementById(`tags-${roleKey}`);
    if ([...tagsDiv.children].some(tag => tag.dataset.value === value)) return;

    const tag = document.createElement('div');
    tag.classList.add('tag');
    tag.dataset.value = value;
    tag.innerHTML = `
            ${value}
            <span class="remove-tag">&times;</span>
            <input type="hidden" name="techStacks_${roleKey}" value="${value}">
        `;
    tag.querySelector('.remove-tag').onclick = () => tag.remove();
    tagsDiv.appendChild(tag);
}