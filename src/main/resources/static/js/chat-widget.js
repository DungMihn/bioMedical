const apiUrl = '/api/chat';

// Mở / đóng panel widget
function toggleChatWidget(force) {
    const panel = document.getElementById('chatWidgetPanel');
    if (!panel) return;

    const visible = panel.style.display === 'flex';

    // Xác định trạng thái muốn set
    let shouldOpen;
    if (typeof force === 'boolean') {
        shouldOpen = force;           // true = mở, false = đóng
    } else {
        shouldOpen = !visible;        // không truyền gì thì đảo trạng thái
    }

    if (shouldOpen) {
        panel.style.display = 'flex';
        // lưu lại là đang mở
        localStorage.setItem('chatWidgetOpen', '1');
    } else {
        panel.style.display = 'none';
        // lưu lại là đang đóng
        localStorage.setItem('chatWidgetOpen', '0');
    }
}


document.addEventListener('DOMContentLoaded', function () {
    const btn = document.getElementById('chatWidgetButton');
    if (btn) {
        btn.addEventListener('click', () => {
            toggleChatWidget(); // click bubble thì đảo trạng thái + lưu
        });
    }

    initChatWidgetInput();
    loadWidgetHistory();

    // 👉 nếu lần trước đang mở, thì tự mở lại
    const savedState = localStorage.getItem('chatWidgetOpen');
    if (savedState === '1') {
        toggleChatWidget(true);   // mở panel nhưng KHÔNG đảo state
    }
});


// Thêm message vào khung chat
function appendWidgetMessage(text, sender) {
    const container = document.getElementById('chatWidgetMessages');
    if (!container) return;

    const wrap = document.createElement('div');
    wrap.classList.add('chat-widget-message', sender);

    const bubble = document.createElement('div');
    bubble.classList.add('chat-widget-bubble');
    bubble.innerText = text;

    wrap.appendChild(bubble);
    container.appendChild(wrap);

    container.scrollTop = container.scrollHeight;
}

// Gửi message
async function sendWidgetMessage(forcedText) {
    const input = document.getElementById('chatWidgetInput');
    if (!input && !forcedText) return;

    const msg = (typeof forcedText === 'string')
        ? forcedText
        : input.value.trim();

    if (!msg) return;

    appendWidgetMessage(msg, 'user');

    if (input && !forcedText) {
        input.value = '';
        input.style.height = '32px';   // trùng với min-height trong CSS
        input.style.overflowY = 'hidden';
    }

    const status = document.getElementById('chatWidgetStatus');
    if (status) status.textContent = 'Đang xử lý...';

    try {
        const res = await fetch(apiUrl, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({message: msg})
        });

        const data = await res.json();
        const reply = data.reply || 'Tôi chưa hiểu.';
        appendWidgetMessage(reply, 'bot');

        // dựng nút slot nếu có
        renderSlotButtonsFromReply(reply);

        if (status) status.textContent = 'Sẵn sàng hỗ trợ';
    } catch (e) {
        appendWidgetMessage('Không thể kết nối.', 'bot');
        if (status) status.textContent = 'Lỗi kết nối';
        console.error(e);
    }
}

// Auto resize textarea + Enter để gửi
function initChatWidgetInput() {
    const ta = document.getElementById('chatWidgetInput');
    if (!ta) return;

    const maxHeight = 96;

    const autoResize = () => {
        ta.style.height = 'auto';
        ta.style.height = Math.min(ta.scrollHeight, maxHeight) + 'px';
        ta.style.overflowY = ta.scrollHeight > maxHeight ? 'auto' : 'hidden';
    };

    autoResize();

    ta.addEventListener('input', autoResize);

    ta.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendWidgetMessage();
        }
    });
}

// ================== VẼ BUTTON KHUNG GIỜ TỪ REPLY TEXT ==================
function renderSlotButtonsFromReply(replyText) {
    if (!replyText) return;

    const lines = replyText.split('\n');
    if (lines.length < 2) return;

    const timesLine = lines[1].trim();
    if (!/^\d{2}:\d{2}/.test(timesLine)) return;

    const times = timesLine
        .split(',')
        .map(t => t.trim())
        .filter(t => t.length > 0);

    if (times.length === 0) return;

    const container = document.getElementById('chatWidgetMessages');
    if (!container) return;

    const wrap = document.createElement('div');
    wrap.classList.add('chat-widget-message', 'bot');

    const box = document.createElement('div');
    box.classList.add('slot-box');

    times.forEach(time => {
        const btn = document.createElement('button');
        btn.classList.add('slot-button');
        btn.textContent = time;
        btn.onclick = () => sendWidgetMessage('Cho tôi khung ' + time);
        box.appendChild(btn);
    });

    wrap.appendChild(box);
    container.appendChild(wrap);
    container.scrollTop = container.scrollHeight;
}

// ================== LOAD LỊCH SỬ CHAT ==================
async function loadWidgetHistory() {
    const container = document.getElementById('chatWidgetMessages');
    if (!container) return;

    try {
        const res = await fetch(apiUrl + '/history', {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        });

        if (!res.ok) {
            console.warn('Không load được lịch sử chat, status = ', res.status);
            return;
        }

        const data = await res.json(); // [{sender, message, time}, ...]

        container.innerHTML = '';

        if (!data || data.length === 0) {
            appendWidgetMessage('Xin chào 👋, tôi có thể giúp gì cho bạn?', 'bot');
            return;
        }

        data.forEach(m => {
            appendWidgetMessage(m.message, m.sender); // sender: "user" | "bot"
        });

    } catch (e) {
        console.error('Lỗi khi load lịch sử chat:', e);
    }
}

// ================== MENU HEADER (giống Messenger) ==================
function toggleChatWidgetMenu(force) {
    const menu = document.getElementById('chatWidgetMenu');
    if (!menu) return;

    const visible = menu.style.display === 'block';

    if (force === false || (visible && !force)) {
        menu.style.display = 'none';
    } else {
        menu.style.display = 'block';
    }
}

function openFullChatPage() {
    // Đổi URL này cho khớp với controller full page của bạn
    window.location.href = '/support/chat';
}

// Click ngoài panel thì tự đóng menu
document.addEventListener('click', function (e) {
    const menu   = document.getElementById('chatWidgetMenu');
    const header = document.querySelector('.chat-widget-header-main');

    if (!menu || !header) return;

    // Nếu menu đang mở
    const isOpen = menu.style.display === 'block';
    if (!isOpen) return;

    // Nếu click KHÔNG nằm trong menu và KHÔNG nằm trong header → đóng menu
    if (!menu.contains(e.target) && !header.contains(e.target)) {
        toggleChatWidgetMenu(false);
    }
});
