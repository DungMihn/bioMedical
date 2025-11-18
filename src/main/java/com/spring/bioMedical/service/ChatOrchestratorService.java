package com.spring.bioMedical.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.bioMedical.entity.AppointmentBooking;
import com.spring.bioMedical.entity.AppointmentSlot;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.AppointmentSlotRepository;
import com.spring.bioMedical.repository.ClinicRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatOrchestratorService {

    private final ChatService chatService;
    private final ScheduleService scheduleService;
    private final AppointmentSlotRepository slotRepo;
    private final ClinicRepository clinicRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ====== TRẠNG THÁI CHỜ XÁC NHẬN THEO USER ======
    private static class PendingBooking {
        Long clinicId;
        LocalDate date;
        Long slotId;
        String note;     // lý do / mô tả khám
    }

    // key = userId
    private final Map<Long, PendingBooking> pendingMap = new ConcurrentHashMap<>();

    public ChatOrchestratorService(ChatService chatService,
                                   ScheduleService scheduleService,
                                   ClinicRepository clinicRepo,
                                   AppointmentSlotRepository slotRepo) {
        this.chatService = chatService;
        this.scheduleService = scheduleService;
        this.clinicRepo = clinicRepo;
        this.slotRepo = slotRepo;
    }

    public String handleUserMessage(String userMessage, Users user) {
        Long userId = user.getUserId();
        String msgLower = userMessage.toLowerCase().trim();

        // 0) Nếu đang có pending và câu này là chọn khung giờ "Cho tôi khung 09:00"
        String timeHandled = handleTimeSelectionIfAny(userMessage, user);
        if (timeHandled != null) {
            return timeHandled;
        }

        // 1) Nếu user ĐANG có booking chờ confirm và họ nói "đồng ý" => tiến hành đặt thật
        if (pendingMap.containsKey(userId)
                && (msgLower.contains("đồng ý")
                || msgLower.contains("dong y")
                || msgLower.contains("ok")
                || msgLower.contains("oke"))) {
            return confirmPendingBooking(user);
        }

        // 1.5) Nếu ĐANG có pending mà câu này KHÔNG phải "đồng ý" & không phải chọn giờ
        //      => coi như user đang nhập LÝ DO KHÁM (description)
        if (pendingMap.containsKey(userId)) {
            PendingBooking pb = pendingMap.get(userId);
            String note = userMessage.trim();
            if (!note.isEmpty()) {
                pb.note = note;
                return "Mình đã ghi chú lý do khám của bạn là: \"" + note + "\".\n"
                        + "Nếu bạn ĐỒNG Ý đặt lịch này, hãy trả lời: \"Đồng ý\".";
            }
        }

        // 2) Ngược lại, xử lý bằng AI như cũ
        try {
            String rawJson = chatService.askAi(userMessage);   // LLM trả JSON

            JsonNode root = objectMapper.readTree(rawJson);
            String intent = root.path("intent").asText("");

            if ("book_appointment".equalsIgnoreCase(intent)) {
                return handleBookingIntent(root, user, userMessage);
            }

            if ("chitchat".equalsIgnoreCase(intent)) {
                return root.path("answer")
                        .asText("Xin lỗi, tôi chưa hiểu câu hỏi của bạn.");
            }

            return "Xin lỗi, tôi chưa hiểu yêu cầu của bạn. "
                    + "Bạn có thể nói rõ hơn bạn muốn hỏi hay đặt lịch gì không?";

        } catch (Exception e) {
            e.printStackTrace();
            return "Có lỗi khi xử lý yêu cầu. Bạn vui lòng thử lại sau.";
        }
    }

    // =========================================================
    //  PHẦN 1: KHI USER NÓI ĐẶT LỊCH (book_appointment INTENT)
    // =========================================================
    private String handleBookingIntent(JsonNode root, Users user, String originalText) {

        long clinicIdFromAi     = root.path("clinicId").asLong(0L);
        String clinicNameFromAi = root.path("clinicName").asText(null);
        String dateStr          = root.path("date").asText(null);
        String timeOfDay        = root.path("time_of_day").asText(null);
        String noteFromAi       = root.path("note").asText(null); // nếu LLM có tách note

        // ⭐ Map lại clinicId từ tên chi nhánh / câu gốc
        long resolvedClinicId = resolveClinicId(clinicIdFromAi, clinicNameFromAi, originalText);

        if (resolvedClinicId <= 0 || dateStr == null || dateStr.isBlank()) {
            return "Để đặt lịch, bạn vui lòng cung cấp rõ chi nhánh và ngày khám nhé.";
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return "Ngày khám không hợp lệ. Vui lòng dùng dạng yyyy-MM-dd (ví dụ: 2025-11-20).";
        }

        // 1. Lấy toàn bộ slot của chi nhánh trong ngày đó
        List<AppointmentSlot> slots =
                slotRepo.findByClinicIdAndSlotDateOrderBySlotTimeAsc(resolvedClinicId, date);

        if (slots.isEmpty()) {
            return "Ngày " + date + " tại " + clinicLabel(resolvedClinicId)
                    + " hiện chưa có khung giờ nào được mở. Bạn vui lòng chọn ngày khác.";
        }

        // 2. Lọc slot AVAILABLE + chưa qua giờ + phù hợp ca
        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        List<AppointmentSlot> available = slots.stream()
                .filter(s -> "AVAILABLE".equalsIgnoreCase(s.getStatus()))
                .filter(s -> {
                    if (s.getSlotDate().isBefore(today)) return false;
                    if (s.getSlotDate().isEqual(today) && s.getSlotTime().isBefore(now)) return false;
                    return true;
                })
                .filter(s -> matchTimeOfDay(s.getSlotTime(), timeOfDay))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            return "Xin lỗi, không còn khung giờ trống phù hợp vào ngày "
                    + date + " tại " + clinicLabel(resolvedClinicId) + ".";
        }

        // 3. Chọn 1 khung giờ GỢI Ý (đầu tiên trong danh sách phù hợp)
        AppointmentSlot chosen = available.get(0);

        // 4. Lưu vào pendingMap CHỨ CHƯA BOOK
        PendingBooking pb = new PendingBooking();
        pb.clinicId = resolvedClinicId;
        pb.date     = chosen.getSlotDate();
        pb.slotId   = chosen.getSlotId();
        pb.note     = (noteFromAi != null && !noteFromAi.trim().isEmpty()) ? noteFromAi.trim() : null;
        pendingMap.put(user.getUserId(), pb);

        // 5. Chuẩn bị list tất cả giờ trống để show cho người dùng lựa (ở dạng text)
        String allTimes = available.stream()
                .map(s -> s.getSlotTime().toString())
                .collect(Collectors.joining(", "));

        StringBuilder sb = new StringBuilder();
        sb.append("Các khung giờ trống ngày ").append(date)
                .append(" tại ").append(clinicLabel(resolvedClinicId)).append(":\n")
                .append(allTimes).append("\n\n");

        sb.append("Mình gợi ý đặt khung giờ ")
                .append(chosen.getSlotTime())
                .append(" cho bạn.\n");

        sb.append("Bạn có thể bấm vào 1 khung giờ bên dưới để chọn cho nhanh.\n");
        sb.append("Sau khi chọn giờ, mình sẽ hỏi thêm về lý do khám để ghi chú vào lịch hẹn của bạn.");

        return sb.toString();
    }

    /**
     * Lọc ca theo "sáng" / "chiều" / "tối".
     * Nếu timeOfDay null hoặc rỗng => chấp nhận mọi khung giờ.
     */
    private boolean matchTimeOfDay(LocalTime time, String timeOfDay) {
        if (timeOfDay == null || timeOfDay.isBlank()) {
            return true;
        }
        String key = timeOfDay.trim().toLowerCase();

        if (key.contains("sáng")) {
            return !time.isBefore(LocalTime.of(5, 0))
                    && time.isBefore(LocalTime.of(12, 0));
        } else if (key.contains("chiều")) {
            return !time.isBefore(LocalTime.of(12, 0))
                    && time.isBefore(LocalTime.of(18, 0));
        } else if (key.contains("tối") || key.contains("đêm")) {
            return !time.isBefore(LocalTime.of(18, 0))
                    && time.isBefore(LocalTime.of(23, 0));
        }
        return true;
    }

    // =========================================================
    //  PHẦN 2: KHI USER TRẢ LỜI "ĐỒNG Ý"  => BOOK THẬT
    // =========================================================
    private String confirmPendingBooking(Users user) {
        Long userId = user.getUserId();
        PendingBooking pb = pendingMap.remove(userId);
        if (pb == null) {
            return "Hiện bạn không có lịch hẹn nào đang chờ xác nhận.";
        }

        // Lấy lại slot từ DB để đảm bảo nó vẫn AVAILABLE
        AppointmentSlot slot = slotRepo.findById(pb.slotId).orElse(null);
        if (slot == null
                || !"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
            return "Xin lỗi, khung giờ bạn vừa chọn đã không còn trống. Bạn vui lòng chọn khung khác.";
        }

        try {
            AppointmentBooking booking = scheduleService.book(
                    userId,
                    pb.slotId,
                    pb.note
            );

            return String.format(
                    "✅ ĐÃ ĐẶT LỊCH THÀNH CÔNG:\n" +
                            "- Chi nhánh: %s\n" +
                            "- Ngày: %s\n" +
                            "- Giờ: %s\n" +
                            "- Mã lịch hẹn: %d\n" +
                            "%s",
                    clinicLabel(pb.clinicId),
                    slot.getSlotDate(),
                    slot.getSlotTime(),
                    booking.getAppointmentId(),
                    (pb.note != null ? "\nGhi chú: " + pb.note : "")
            );

        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, có lỗi khi tạo lịch hẹn. Bạn vui lòng thử lại hoặc chọn khung giờ khác.";
        }
    }

    // ================== HIỂN THỊ LABEL CHI NHÁNH ĐẸP HƠN ==================
    private String clinicLabel(long clinicId) {
        return clinicRepo.findById(clinicId)
                .map(c -> c.getName())
                .orElse("chi nhánh ID " + clinicId);
    }

    // ================== MAP TÊN CHI NHÁNH → clinic_id ==================
    private Long resolveClinicId(long clinicIdFromAi,
                                 String clinicNameFromAi,
                                 String originalText) {

        // 1) Nếu AI đã trả số hợp lệ (1,2,3...) thì vẫn ưu tiên dùng
        if (clinicIdFromAi > 0) {
            return clinicIdFromAi;
        }

        // 2) Nếu có clinicName trong JSON, dùng nó, ngược lại dùng originalText
        String text = null;
        if (clinicNameFromAi != null && !clinicNameFromAi.isBlank()) {
            text = clinicNameFromAi;
        } else if (originalText != null) {
            text = originalText;
        } else {
            return 0L;
        }

        String t = text.toLowerCase();

        // Hà Nội
        if (t.contains("hà nội") || t.contains("ha noi")) {
            return clinicRepo.findByNameContainingIgnoreCase("Hà Nội")
                    .stream()
                    .findFirst()
                    .map(c -> c.getClinicId())
                    .orElse(1L); // fallback cứng: 1
        }
        // Sài Gòn / TP HCM
        if (t.contains("sài gòn") || t.contains("sai gon")
                || t.contains("hồ chí minh") || t.contains("tp hcm")) {
            return clinicRepo.findByNameContainingIgnoreCase("Sài Gòn")
                    .stream()
                    .findFirst()
                    .map(c -> c.getClinicId())
                    .orElse(2L);
        }
        // Đà Nẵng
        if (t.contains("đà nẵng") || t.contains("da nang")) {
            return clinicRepo.findByNameContainingIgnoreCase("Đà Nẵng")
                    .stream()
                    .findFirst()
                    .map(c -> c.getClinicId())
                    .orElse(3L);
        }

        // 3) Nếu user gõ nguyên tên đầy đủ thì match trực tiếp
        return clinicRepo.findByNameContainingIgnoreCase(text)
                .stream()
                .findFirst()
                .map(c -> c.getClinicId())
                .orElse(0L);
    }

    // ================== XỬ LÝ CÂU "CHO TÔI KHUNG 10:00" ==================
    private String handleTimeSelectionIfAny(String originalMessage, Users user) {
        PendingBooking pb = pendingMap.get(user.getUserId());
        if (pb == null) {
            return null; // không có booking đang chờ thì bỏ qua
        }

        // Tìm pattern giờ hh:mm trong câu
        Matcher m = Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(originalMessage);
        if (!m.find()) {
            return null; // không có giờ -> để AI xử lý như bình thường
        }

        int hour = Integer.parseInt(m.group(1));
        int minute = Integer.parseInt(m.group(2));
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }

        LocalTime target = LocalTime.of(hour, minute);

        // Lấy tất cả slot của chi nhánh + ngày đang pending
        List<AppointmentSlot> slots =
                slotRepo.findByClinicIdAndSlotDateOrderBySlotTimeAsc(pb.clinicId, pb.date);
        if (slots.isEmpty()) {
            return "Ngày " + pb.date + " tại " + clinicLabel(pb.clinicId) +
                    " hiện không có khung giờ nào được mở. Bạn vui lòng chọn ngày khác.";
        }

        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        AppointmentSlot chosen = slots.stream()
                .filter(s -> "AVAILABLE".equalsIgnoreCase(s.getStatus()))
                .filter(s -> {
                    if (s.getSlotDate().isBefore(today)) return false;
                    if (s.getSlotDate().isEqual(today) && s.getSlotTime().isBefore(now)) return false;
                    return true;
                })
                .filter(s -> s.getSlotTime().equals(target))
                .findFirst()
                .orElse(null);

        if (chosen == null) {
            return "Ở ngày " + pb.date + " tại " + clinicLabel(pb.clinicId) +
                    " không có khung " + target + " còn trống. Bạn vui lòng chọn giờ khác.";
        }

        // Cập nhật slotId được chọn
        pb.slotId = chosen.getSlotId();

        StringBuilder sb = new StringBuilder();
        sb.append("Mình đã đổi sang khung giờ ").append(target)
                .append(" ngày ").append(pb.date)
                .append(" tại ").append(clinicLabel(pb.clinicId)).append(".\n");

        if (pb.note == null || pb.note.isBlank()) {
            sb.append("Bây giờ bạn vui lòng cho mình biết lý do / triệu chứng khám ")
                    .append("(ví dụ: đau họng, khám tổng quát, kiểm tra định kỳ...).\n");
        } else {
            sb.append("Nếu bạn ĐỒNG Ý đặt lịch này, hãy trả lời: \"Đồng ý\".");
        }

        return sb.toString();
    }
}
