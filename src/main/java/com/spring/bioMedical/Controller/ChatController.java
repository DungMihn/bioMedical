package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.ChatOrchestratorService;
import com.spring.bioMedical.service.UsersService;
import com.spring.bioMedical.service.ChatHistoryService;
import com.spring.bioMedical.entity.ChatMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatOrchestratorService orchestratorService;
    private final UsersService usersService;
    private final ChatHistoryService chatHistoryService;

    public ChatController(ChatOrchestratorService orchestratorService,
                          UsersService usersService,
                          ChatHistoryService chatHistoryService) {
        this.orchestratorService = orchestratorService;
        this.usersService = usersService;
        this.chatHistoryService = chatHistoryService;
    }

    // ====== DTO request / response cho API chat ======

    public static class ChatRequest {
        private String message;

        public ChatRequest() {
        }

        public ChatRequest(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ChatResponse {
        private String reply;

        public ChatResponse() {
        }

        public ChatResponse(String reply) {
            this.reply = reply;
        }

        public String getReply() {
            return reply;
        }

        public void setReply(String reply) {
            this.reply = reply;
        }
    }

    // DTO để trả lịch sử về cho JS
    public static class ChatMessageDto {
        private String sender; // "user" hoặc "bot"
        private String message;
        private String time;   // HH:mm dd/MM nếu cần hiển thị

        public ChatMessageDto() {
        }

        public ChatMessageDto(String sender, String message, String time) {
            this.sender = sender;
            this.message = message;
            this.time = time;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    // ====== helper: lấy user hiện tại ======

    private Users getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return usersService.findByUsername(principal.getName());
    }

    // ====== endpoint chính /api/chat (gửi tin nhắn) ======

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request,
                                             Principal principal) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Vui lòng nhập nội dung cần hỏi."));
        }

        String userMessage = request.getMessage().trim();
        Users user = getCurrentUser(principal);

        // lưu tin nhắn USER
        if (user != null) {
            chatHistoryService.saveUserMessage(user, userMessage);
        }

        // Giao cho Orchestrator xử lý (AI + đặt lịch nếu cần)
        String answer = orchestratorService.handleUserMessage(userMessage, user);

        // lưu tin nhắn BOT
        if (user != null) {
            chatHistoryService.saveBotMessage(user, answer);
        }

        return ResponseEntity.ok(new ChatResponse(answer));
    }

    // ====== endpoint GET /api/chat/history (lấy lịch sử chat) ======

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDto>> getHistory(Principal principal) {
        Users user = getCurrentUser(principal);
        if (user == null) {
            // nếu chưa login thì trả rỗng
            return ResponseEntity.ok(List.of());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm dd/MM");

        List<ChatMessage> history = chatHistoryService.getHistory(user);

        List<ChatMessageDto> dtos = history.stream()
                .map(m -> new ChatMessageDto(
                        "USER".equalsIgnoreCase(m.getSender()) ? "user" : "bot",
                        m.getMessage(),
                        m.getCreatedAt() != null ? m.getCreatedAt().format(fmt) : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
