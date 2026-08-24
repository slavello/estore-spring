'use strict';

// ============================== УТИЛИТЫ ==============================

const moneyFmt = new Intl.NumberFormat('ru-RU', {
    style: 'currency', currency: 'RUB', maximumFractionDigits: 2
});

function fmtMoney(v) {
    return v == null ? '' : moneyFmt.format(Number(v));
}

function fmtDate(v) {
    if (!v) return '';
    const d = new Date(v);
    return isNaN(d) ? v : d.toLocaleDateString('ru-RU');
}

function fmtDateTime(v) {
    if (!v) return '';
    const d = new Date(v);
    return isNaN(d) ? v : d.toLocaleString('ru-RU');
}

function esc(s) {
    return s == null ? '' : String(s)
        .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}

async function api(url, options = {}) {
    const opts = Object.assign({ headers: {} }, options);
    if (opts.body && !(opts.body instanceof FormData)) {
        opts.headers['Content-Type'] = 'application/json';
    }
    const res = await fetch(url, opts);
    if (!res.ok) {
        let msg = 'Ошибка ' + res.status;
        try {
            const data = await res.json();
            if (data && data.message) msg = data.message;
        } catch (e) { /* тело не JSON */ }
        const err = new Error(msg);
        err.status = res.status;
        throw err;
    }
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

function toast(message, type) {
    const box = document.getElementById('toasts');
    const el = document.createElement('div');
    el.className = 'toast ' + (type || '');
    el.textContent = message;
    el.onclick = () => el.remove();
    box.appendChild(el);
    setTimeout(() => el.remove(), 5000);
}

// ============================== КОНФИГУРАЦИЯ РАЗДЕЛОВ ==============================

const RESOURCES = {
    employees: {
        title: 'Сотрудники',
        endpoint: '/api/employees',
        importUrl: '/api/import/employees',
        columns: [
            { key: 'lastName', label: 'Фамилия' },
            { key: 'firstName', label: 'Имя' },
            { key: 'middleName', label: 'Отчество' },
            { key: 'birthDate', label: 'Дата рождения', fmt: fmtDate },
            { key: 'positionName', label: 'Должность' },
            { key: 'shopName', label: 'Магазин' },
            { key: 'male', label: 'Пол', fmt: v => v === true ? 'М' : (v === false ? 'Ж' : '') }
        ],
        fields: [
            { name: 'lastName', label: 'Фамилия', type: 'text', required: true, max: 100 },
            { name: 'firstName', label: 'Имя', type: 'text', required: true, max: 100 },
            { name: 'middleName', label: 'Отчество', type: 'text', max: 100 },
            { name: 'birthDate', label: 'Дата рождения', type: 'date', required: true },
            { name: 'positionId', label: 'Должность', type: 'select', url: '/api/positions?size=200', labelFn: i => i.name, required: true },
            { name: 'shopId', label: 'Магазин', type: 'select', url: '/api/shops?size=200', labelFn: i => i.name, required: true },
            {
                name: 'male', label: 'Пол', type: 'select-static', required: true,
                options: [{ v: true, l: 'Мужской' }, { v: false, l: 'Женский' }]
            }
        ]
    },
    electronics: {
        title: 'Электротовары',
        endpoint: '/api/electronics',
        importUrl: '/api/import/electronics',
        columns: [
            { key: 'name', label: 'Название' },
            { key: 'typeName', label: 'Тип' },
            { key: 'price', label: 'Цена, руб.', fmt: fmtMoney },
            { key: 'quantity', label: 'Кол-во' },
            { key: 'archived', label: 'В продаже', fmt: v => v === true ? 'нет' : 'да' },
            { key: 'description', label: 'Описание', truncate: 80 }
        ],
        fields: [
            { name: 'name', label: 'Название товара', type: 'text', required: true, max: 150 },
            { name: 'typeId', label: 'Тип товара', type: 'select', url: '/api/electronics-types?size=200', labelFn: i => i.name, required: true },
            { name: 'price', label: 'Цена, руб.', type: 'number', step: '0.01', min: '0' },
            { name: 'quantity', label: 'Общее количество в магазинах', type: 'number', min: '0' },
            { name: 'archived', label: 'Архивный (снят с продаж)', type: 'checkbox' },
            { name: 'description', label: 'Описание', type: 'textarea' }
        ]
    },
    purchases: {
        title: 'Покупки',
        endpoint: '/api/purchases',
        importUrl: '/api/import/purchases',
        sortable: [
            { v: 'dateTime,desc', l: 'Дата покупки ↓ (сначала новые)' },
            { v: 'dateTime,asc', l: 'Дата покупки ↑ (сначала старые)' }
        ],
        columns: [
            { key: 'dateTime', label: 'Дата/время покупки', fmt: fmtDateTime },
            { key: 'electronicsName', label: 'Товар' },
            { key: 'employeeName', label: 'Сотрудник' },
            { key: 'shopName', label: 'Магазин' },
            { key: 'purchaseTypeName', label: 'Тип покупки' },
            { key: 'price', label: 'Сумма, руб.', fmt: fmtMoney }
        ],
        fields: [
            { name: 'electronicsId', label: 'Электротовар', type: 'select', url: '/api/electronics?size=500', labelFn: i => i.name + ' (' + fmtMoney(i.price) + ')', required: true },
            { name: 'employeeId', label: 'Сотрудник', type: 'select', url: '/api/employees?size=500', labelFn: i => [i.lastName, i.firstName, i.middleName].filter(Boolean).join(' '), required: true },
            { name: 'shopId', label: 'Магазин', type: 'select', url: '/api/shops?size=200', labelFn: i => i.name, required: true },
            { name: 'purchaseTypeId', label: 'Тип покупки', type: 'select', url: '/api/purchase-types?size=100', labelFn: i => i.name, required: true },
            { name: 'dateTime', label: 'Дата/время покупки (пусто — текущее время)', type: 'datetime-local' }
        ]
    },
    stocks: {
        title: 'Наличие товаров в магазинах',
        endpoint: '/api/stock',
        importUrl: '/api/import/stock',
        columns: [
            { key: 'electronicsName', label: 'Товар' },
            { key: 'shopName', label: 'Магазин' },
            { key: 'quantity', label: 'Количество в наличии' }
        ],
        fields: [
            { name: 'electronicsId', label: 'Электротовар', type: 'select', url: '/api/electronics?size=500', labelFn: i => i.name, required: true },
            { name: 'shopId', label: 'Магазин', type: 'select', url: '/api/shops?size=200', labelFn: i => i.name, required: true },
            { name: 'quantity', label: 'Количество', type: 'number', min: '0', required: true }
        ],
        noEdit: false
    },
    positions: dictSection('Должности', '/api/positions', '/api/import/positions'),
    etypes: dictSection('Типы электроники', '/api/electronics-types', '/api/import/electronics-types'),
    ptypes: dictSection('Типы покупок', '/api/purchase-types', '/api/import/purchase-types'),
    shops: {
        title: 'Магазины',
        endpoint: '/api/shops',
        importUrl: '/api/import/shops',
        columns: [
            { key: 'name', label: 'Наименование' },
            { key: 'address', label: 'Адрес', truncate: 120 }
        ],
        fields: [
            { name: 'name', label: 'Наименование', type: 'text', required: true, max: 150 },
            { name: 'address', label: 'Адрес', type: 'textarea' }
        ]
    }
};

function dictSection(title, endpoint, importUrl) {
    return {
        title: title,
        endpoint: endpoint,
        importUrl: importUrl,
        columns: [{ key: 'name', label: 'Наименование' }],
        fields: [{ name: 'name', label: 'Наименование', type: 'text', required: true, max: 150 }]
    };
}

// ============================== ПОСТРОЕНИЕ СЕКЦИЙ ==============================

const state = {};

function buildSections() {
    const root = document.getElementById('sections');
    for (const [key, cfg] of Object.entries(RESOURCES)) {
        state[key] = { page: 0, size: 10 };

        const section = document.createElement('section');
        section.id = 'section-' + key;
        section.className = 'section hidden';

        const h = document.createElement('h2');
        h.textContent = cfg.title;
        section.appendChild(h);

        // панель инструментов
        const toolbar = document.createElement('div');
        toolbar.className = 'toolbar';

        const addBtn = document.createElement('button');
        addBtn.className = 'primary';
        addBtn.textContent = '+ Добавить';
        addBtn.onclick = () => openDialog(key, null);
        toolbar.appendChild(addBtn);

        // импорт CSV-файла для этой таблицы
        if (cfg.importUrl) {
            const importBtn = document.createElement('button');
            importBtn.className = 'secondary';
            importBtn.title = 'Импортировать записи из CSV-файла';
            importBtn.textContent = '⇩ Импорт CSV';
            const fileInput = document.createElement('input');
            fileInput.type = 'file';
            fileInput.accept = '.csv,text/csv';
            fileInput.style.display = 'none';
            importBtn.onclick = () => fileInput.click();
            fileInput.onchange = () => importCsv(key, fileInput);
            toolbar.appendChild(importBtn);
            toolbar.appendChild(fileInput);
        }

        const sizeWrap = document.createElement('label');
        sizeWrap.className = 'muted';
        sizeWrap.innerHTML = 'Строк на странице: ';
        const sizeSel = document.createElement('select');
        [5, 10, 20, 50].forEach(n => {
            const o = document.createElement('option');
            o.value = n; o.textContent = n;
            if (n === 10) o.selected = true;
            sizeSel.appendChild(o);
        });
        sizeSel.onchange = () => { state[key].size = Number(sizeSel.value); state[key].page = 0; loadList(key); };
        sizeWrap.appendChild(sizeSel);
        toolbar.appendChild(sizeWrap);

        if (cfg.sortable) {
            const sortWrap = document.createElement('label');
            sortWrap.className = 'muted';
            sortWrap.innerHTML = 'Сортировка: ';
            const sortSel = document.createElement('select');
            cfg.sortable.forEach(o => {
                const op = document.createElement('option');
                op.value = o.v; op.textContent = o.l;
                sortSel.appendChild(op);
            });
            sortSel.onchange = () => { state[key].sort = sortSel.value; loadList(key); };
            sortWrap.appendChild(sortSel);
            toolbar.appendChild(sortWrap);
        }

        section.appendChild(toolbar);

        // таблица
        const tableWrap = document.createElement('div');
        tableWrap.id = 'table-' + key;
        section.appendChild(tableWrap);

        // результат импорта CSV
        const csvResult = document.createElement('div');
        csvResult.id = 'csv-result-' + key;
        section.appendChild(csvResult);

        // пагинация
        const pager = document.createElement('div');
        pager.className = 'pager';
        pager.id = 'pager-' + key;
        section.appendChild(pager);

        root.appendChild(section);
    }
}

function showSection(key) {
    document.querySelectorAll('.nav-item').forEach(b =>
        b.classList.toggle('active', b.dataset.section === key));
    document.querySelectorAll('.section').forEach(s => s.classList.add('hidden'));
    const target = key === 'reports' || key === 'import'
        ? document.getElementById('section-' + key)
        : document.getElementById('section-' + key);
    target.classList.remove('hidden');
    if (RESOURCES[key]) loadList(key);
}

// ============================== ЗАГРУЗКА СПИСКОВ ==============================

async function loadList(key) {
    const cfg = RESOURCES[key];
    const st = state[key];
    let url = `${cfg.endpoint}?page=${st.page}&size=${st.size}`;
    if (st.sort) url += `&sort=${encodeURIComponent(st.sort)}`;

    try {
        const page = await api(url);
        renderTable(key, cfg, page);
        renderPager(key, page);
    } catch (e) {
        toast('Не удалось загрузить список: ' + e.message, 'err');
    }
}

function renderTable(key, cfg, page) {
    const wrap = document.getElementById('table-' + key);
    if (!page.content.length) {
        wrap.innerHTML = '<p class="muted">Записей нет.</p>';
        return;
    }
    let html = '<table class="data"><thead><tr><th>#</th>';
    cfg.columns.forEach(c => html += `<th>${esc(c.label)}</th>`);
    html += '<th></th></tr></thead><tbody>';
    page.content.forEach(row => {
        html += `<tr><td class="muted">${row.id}</td>`;
        cfg.columns.forEach(c => {
            let val = c.fmt ? c.fmt(row[c.key]) : row[c.key];
            if (c.truncate && val != null && String(val).length > c.truncate) {
                val = String(val).substring(0, c.truncate) + '…';
            }
            html += `<td>${esc(val)}</td>`;
        });
        html += `<td class="actions">
            <button class="secondary small" onclick="editRow('${key}',${row.id})">Изменить</button>
            <button class="small danger" onclick="deleteRow('${key}',${row.id})">Удалить</button>
        </td></tr>`;
    });
    html += '</tbody></table>';
    wrap.innerHTML = html;

    // кэш строк текущей страницы для кнопки «Изменить»
    window['_rows_' + key] = {};
    page.content.forEach(r => window['_rows_' + key][r.id] = r);
}

function renderPager(key, page) {
    const pager = document.getElementById('pager-' + key);
    pager.innerHTML = '';
    const prev = document.createElement('button');
    prev.className = 'secondary small';
    prev.textContent = '‹ Назад';
    prev.disabled = page.first;
    prev.onclick = () => { state[key].page--; loadList(key); };

    const next = document.createElement('button');
    next.className = 'secondary small';
    next.textContent = 'Вперёд ›';
    next.disabled = page.last;
    next.onclick = () => { state[key].page++; loadList(key); };

    const info = document.createElement('span');
    info.className = 'muted';
    info.textContent = `стр. ${page.number + 1} из ${Math.max(page.totalPages, 1)} · всего ${page.totalElements}`;

    pager.appendChild(prev);
    pager.appendChild(next);
    pager.appendChild(info);
}

window.editRow = function (key, id) {
    const row = (window['_rows_' + key] || {})[id];
    openDialog(key, row || null, id);
};

window.deleteRow = async function (key, id) {
    if (!confirm('Удалить запись #' + id + '?')) return;
    try {
        await api(`${RESOURCES[key].endpoint}/${id}`, { method: 'DELETE' });
        toast('Запись удалена', 'ok');
        loadList(key);
    } catch (e) {
        toast('Не удалось удалить: ' + e.message, 'err');
    }
};

// ============================== ДИАЛОГ ДОБАВЛЕНИЯ / РЕДАКТИРОВАНИЯ ==============================

const selectCache = {};

async function loadSelectOptions(url) {
    if (selectCache[url]) return selectCache[url];
    const data = await api(url);
    selectCache[url] = data.content || data;
    return selectCache[url];
}

async function openDialog(key, dto, editId) {
    const cfg = RESOURCES[key];
    const dialog = document.getElementById('edit-dialog');
    const fieldsBox = document.getElementById('dialog-fields');
    document.getElementById('dialog-title').textContent =
        (editId ? 'Изменить: ' : 'Добавить: ') + cfg.title;
    fieldsBox.innerHTML = '';

    for (const f of cfg.fields) {
        const field = document.createElement('div');
        field.className = 'field';
        const label = document.createElement('span');
        label.textContent = f.label + (f.required ? ' *' : '');
        field.appendChild(label);

        let input;
        if (f.type === 'select') {
            input = document.createElement('select');
            try {
                const items = await loadSelectOptions(f.url);
                const empty = document.createElement('option');
                empty.value = '';
                empty.textContent = '- выберите -';
                input.appendChild(empty);
                items.forEach(item => {
                    const o = document.createElement('option');
                    o.value = item.id;
                    o.textContent = f.labelFn(item);
                    input.appendChild(o);
                });
            } catch (e) {
                toast('Не удалось загрузить справочник для поля \"' + f.label + '\": ' + e.message, 'err');
            }
        } else if (f.type === 'select-static') {
            input = document.createElement('select');
            const empty = document.createElement('option');
            empty.value = '';
            empty.textContent = '- выберите -';
            input.appendChild(empty);
            f.options.forEach(o => {
                const op = document.createElement('option');
                op.value = o.v;
                op.textContent = o.l;
                input.appendChild(op);
            });
        } else if (f.type === 'textarea') {
            input = document.createElement('textarea');
        } else {
            input = document.createElement('input');
            input.type = f.type === 'number' ? 'number' : (f.type === 'date' ? 'date'
                : (f.type === 'datetime-local' ? 'datetime-local' : (f.type === 'checkbox' ? 'checkbox' : 'text')));
            if (f.step) input.step = f.step;
            if (f.min !== undefined) input.min = f.min;
            if (f.max) input.maxLength = f.max;
        }

        input.dataset.name = f.name;
        input.dataset.ftype = f.type;

        // предзаполнение
        if (dto && f.name in dto) {
            const v = dto[f.name];
            if (f.type === 'checkbox') {
                input.checked = !!v;
            } else if (f.type === 'datetime-local') {
                input.value = v ? String(v).substring(0, 16) : '';
            } else if (f.type === 'date') {
                input.value = v || '';
            } else {
                input.value = v == null ? '' : v;
            }
        }

        field.appendChild(input);
        fieldsBox.appendChild(field);
    }

    dialog.dataset.key = key;
    dialog.dataset.editId = editId || '';
    dialog.showModal();

    document.getElementById('btn-cancel').onclick = () => dialog.close();

    document.getElementById('edit-form').onsubmit = async function (ev) {
        ev.preventDefault();
        await submitDialog();
        return false;
    };
}

async function submitDialog() {
    const dialog = document.getElementById('edit-dialog');
    const key = dialog.dataset.key;
    const editId = dialog.dataset.editId;
    const cfg = RESOURCES[key];

    const body = {};
    for (const input of document.querySelectorAll('#dialog-fields [data-name]')) {
        const name = input.dataset.name;
        const type = input.dataset.ftype;
        if (type === 'checkbox') {
            body[name] = input.checked;
        } else if (input.value === '') {
            body[name] = null;
        } else if (type === 'number') {
            body[name] = Number(input.value);
        } else {
            body[name] = input.value;
        }
    }

    // простая клиентская проверка обязательных полей
    for (const f of cfg.fields) {
        if (f.required && (body[f.name] === null || body[f.name] === undefined || body[f.name] === '')) {
            toast('Заполните поле \"' + f.label + '\"', 'err');
            return;
        }
    }

    try {
        if (editId) {
            await api(`${cfg.endpoint}/${editId}`, { method: 'PUT', body: JSON.stringify(body) });
            toast('Запись сохранена', 'ok');
        } else {
            await api(cfg.endpoint, { method: 'POST', body: JSON.stringify(body) });
            toast('Запись добавлена', 'ok');
        }
        dialog.close();
        loadList(key);
    } catch (e) {
        // всплывающая подсказка с сообщением об ошибке
        // (в т.ч. «товара нет в наличии — покупка не оформлена»)
        toast(e.message, 'err');
    }
}

// ============================== ОТЧЁТЫ ==============================

function initReports() {
    const bestBtn = document.getElementById('btn-best');
    bestBtn.onclick = async () => {
        const criterion = document.getElementById('best-criterion').value;
        const out = document.getElementById('best-result');
        try {
            const rows = await api('/api/reports/best-employees?criterion=' + criterion);
            out.innerHTML = renderBestEmployees(rows);
        } catch (e) {
            toast(e.message, 'err');
        }
    };

    document.getElementById('btn-watch').onclick = async () => {
        const out = document.getElementById('watch-result');
        try {
            const rows = await api('/api/reports/best-smartwatch-seller');
            if (!rows.length) {
                out.innerHTML = '<p class="muted">Данных нет: продажи умных часов младшими продавцами-консультантами не найдены.</p>';
                return;
            }
            out.innerHTML = renderBestEmployees(rows);
        } catch (e) {
            toast(e.message, 'err');
        }
    };

    const cashShop = document.getElementById('cash-shop');
    loadSelectOptions('/api/shops?size=200').then(items => {
        items.forEach(i => {
            const o = document.createElement('option');
            o.value = i.id;
            o.textContent = i.name;
            cashShop.appendChild(o);
        });
    }).catch(() => {});

    document.getElementById('btn-cash').onclick = async () => {
        const out = document.getElementById('cash-result');
        const shopId = cashShop.value;
        try {
            const r = await api('/api/reports/cash-total' + (shopId ? '?shopId=' + shopId : ''));
            const place = r.shopName ? esc(r.shopName) : 'вся сеть магазинов';
            out.innerHTML = `<p>Получено наличными (<b>${place}</b>): <span class="big-amount">${fmtMoney(r.amount)}</span></p>`;
        } catch (e) {
            toast(e.message, 'err');
        }
    };
}

function renderBestEmployees(rows) {
    if (!rows.length) {
        return '<p class="muted">Данных нет: продажи за последний год не найдены.</p>';
    }
    let html = '<table class="data"><thead><tr>' +
        '<th>Должность</th><th>Лучший сотрудник</th>' +
        '<th>Кол-во проданных товаров (за последний год)</th>' +
        '<th>Сумма проданных товаров за последний год, руб.</th>' +
        '</tr></thead><tbody>';
    rows.forEach(r => {
        html += `<tr><td>${esc(r.positionName)}</td><td>${esc(r.fullName)}</td>` +
            `<td>${r.soldCount}</td><td>${fmtMoney(r.soldSum)}</td></tr>`;
    });
    html += '</tbody></table>';
    return html;
}

// ============================== ИМПОРТ ==============================

function initImport() {
    document.getElementById('btn-import').onclick = async () => {
        const input = document.getElementById('import-file');
        const out = document.getElementById('import-result');
        if (!input.files.length) {
            toast('Выберите zip-архив с CSV-файлами', 'err');
            return;
        }
        const fd = new FormData();
        fd.append('file', input.files[0]);
        try {
            const result = await api('/api/import', { method: 'POST', body: fd });
            renderImportResult(result);
            if (result.success !== false) {
                toast(result.message || 'Импорт выполнен', 'ok');
            } else {
                toast(result.message || 'Импорт выполнен с ошибками', 'err');
            }
        } catch (e) {
            toast('Ошибка импорта: ' + e.message, 'err');
            out.innerHTML = '';
        }
    };
}

function renderImportResult(result) {
    document.getElementById('import-result').innerHTML = renderImportReportHtml(result);
}

async function importCsv(key, input) {
    if (!input.files || !input.files.length) return;
    const cfg = RESOURCES[key];
    const out = document.getElementById('csv-result-' + key);
    const fd = new FormData();
    fd.append('file', input.files[0]);
    input.value = '';
    try {
        const result = await api(cfg.importUrl, { method: 'POST', body: fd });
        out.innerHTML = renderImportReportHtml(result);
        if (result.success !== false) {
            toast(result.message || 'Импорт выполнен', 'ok');
        } else {
            toast(result.message || 'Импорт выполнен с ошибками', 'err');
        }
        state[key].page = 0;
        loadList(key);
    } catch (e) {
        toast('Ошибка импорта: ' + e.message, 'err');
        out.innerHTML = '';
    }
}

function renderImportReportHtml(result) {
    let html = `<div class="import-report"><p><b>${esc(result.message || '')}</b></p>`;
    if (result.message && !(result.files && result.files.length)) {
        return html + '</div>';
    }
    (result.files || []).forEach(f => {
        html += `<p><b>${esc(f.file)}</b>: добавлено ${f.added}, обновлено ${f.updated}, ошибок ${f.skippedErrors}</p>`;
        if (f.errors && f.errors.length) {
            html += '<ul>' + f.errors.map(e => `<li style="color:#dc2626">${esc(e)}</li>`).join('') + '</ul>';
        }
    });
    return html + '</div>';
}

// ============================== ИНИЦИАЛИЗАЦИЯ ==============================

document.addEventListener('DOMContentLoaded', () => {
    buildSections();
    initReports();
    initImport();

    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.addEventListener('click', () => showSection(btn.dataset.section));
    });

    showSection('employees');
});