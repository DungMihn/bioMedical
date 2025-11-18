package com.spring.bioMedical.service;

import com.spring.bioMedical.form.OllamaRequest;
import com.spring.bioMedical.form.OllamaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatServiceImplementation implements ChatService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ollama.api.url}")
    private String apiUrl;       // vd: http://localhost:11434/api/generate

    @Value("${ollama.model}")
    private String model;        // vd: llama3, mistral...

    @Override
    public String askAi(String message) {
        try {

            // ===============================
            // SYSTEM PROMPT CHUẨN DÀNH CHO AI
            // (Java 8-compatible string)
            // ===============================
            String systemPrompt
                    = "Bạn là trợ lý ảo của hệ thống phòng khám Doctor+.\n\n"
                    + "NHIỆM VỤ:\n"
                    + "1) Nếu người dùng muốn ĐẶT LỊCH KHÁM (có các từ như \"đặt lịch\", \"đặt hẹn\", \"đặt lịch khám\", \"book lịch\", \"đăng ký khám\"...),\n"
                    + "   hãy TRẢ VỀ DUY NHẤT một JSON theo đúng định dạng sau:\n"
                    + "{\n"
                    + "  \"intent\": \"book_appointment\",\n"
                    + "  \"clinicId\": <số nguyên id chi nhánh hoặc 0 nếu không biết>,\n"
                    + "  \"date\": \"<ngày dạng YYYY-MM-DD hoặc rỗng nếu chưa biết>\",\n"
                    + "  \"time_of_day\": \"<sáng | chiều | tối hoặc rỗng nếu chưa rõ>\",\n"
                    + "  \"note\": \"<ghi chú về lý do khám, triệu chứng, chuyên khoa, ...>\"\n"
                    + "}\n\n"
                    + "2) Nếu người dùng CHỈ HỎI THÔNG TIN (không yêu cầu đặt lịch), trả về:\n"
                    + "{\n"
                    + "  \"intent\": \"chitchat\",\n"
                    + "  \"answer\": \"<câu trả lời bằng tiếng Việt>\"\n"
                    + "}\n\n"
                    + "YÊU CẦU QUAN TRỌNG:\n"
                    + "- LUÔN trả về CHỈ MỘT JSON duy nhất.\n"
                    + "- KHÔNG được thêm text, markdown, ```json hoặc ký tự thừa bên ngoài JSON.\n"
                    + "Danh sách chi nhánh hiện có:\n"
                    + "\n"
                    + "1: \"Phòng khám Đa khoa Hà Nội\" (gọi tắt: \"Hà Nội\")\n"
                    + "2: \"Phòng khám Quốc tế Sài Gòn\" (gọi tắt: \"Sài Gòn\", \"TP HCM\", \"Hồ Chí Minh\")\n"
                    + "3: \"Phòng khám Đà Nẵng\" (gọi tắt: \"Đà Nẵng\")\n"
                    + "\n"
                    + "Khi user nói chi nhánh bằng tên, hãy cố gắng điền đúng clinicId theo danh sách trên.";

            // ===============================
            // GHÉP PROMPT
            // ===============================
            String finalPrompt
                    = systemPrompt
                    + "\n\n---\n"
                    + "Nội dung người dùng:\n"
                    + message + "\n";

            // Chuẩn bị body request cho Ollama API
            OllamaRequest req = new OllamaRequest();
            req.setModel(model);
            req.setPrompt(finalPrompt);
            req.setStream(false);

            // Gửi request
            OllamaResponse response = restTemplate.postForObject(
                    apiUrl,
                    req,
                    OllamaResponse.class
            );

            // Nếu AI trả JSON thì trả về thẳng cho Orchestrator
            if (response != null && response.getResponse() != null) {
                return response.getResponse().trim();
            }

            // Fallback: trả JSON mặc định
            return "{ \"intent\": \"chitchat\", \"answer\": \"Hệ thống tạm thời không phản hồi, bạn thử lại sau nhé.\" }";

        } catch (Exception e) {
            e.printStackTrace();
            return "{ \"intent\": \"chitchat\", \"answer\": \"Có lỗi khi kết nối AI. Vui lòng thử lại sau.\" }";
        }
    }
}
