const apiUrl = '/api/chat';

function appendMessage(text, sender) {
    const container = document.getElementById('chatMessages');
    const wrapper = document.createElement('div');
    wrapper.classList.add('chat-message', sender);

    const bubble = document.createElement('div');
    bubble.classList.add('chat-bubble');
    bubble.innerText = text;

    wrapper.appendChild(bubble);
    container.appendChild(wrapper);

    container.scrollTop = container.scrollHeight;
}

function setStatus(text) {
    document.getElementById('chatStatus').textContent = text;
}

async function sendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    if (!message) return;

    appendMessage(message, 'user');
    input.value = '';

    setStatus('Đang xử lý...');

    try {
        const response = await fetch(apiUrl, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ message })
        });

        const data = await response.json();
        appendMessage(data.reply, 'bot');
        setStatus('Sẵn sàng hỗ trợ');
    } catch {
        appendMessage('Lỗi kết nối tới server.', 'bot');
        setStatus('Lỗi');
    }
}

function sendSuggestion(text) {
    document.getElementById('chatInput').value = text;
    sendMessage();
}
