package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.entity.Prescriptions;
import com.spring.bioMedical.entity.Users;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class PDFBoxReportService {

    public byte[] generateMedicalRecordReport(MedicalRecords medicalRecord,
            Users patient, Users doctor, List<Prescriptions> prescriptions) {

        System.out.println("=== BẮT ĐẦU TẠO PDF VỚI PDFBox ===");

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            // Sử dụng font chuẩn
            PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
            PDType1Font fontNormal = PDType1Font.HELVETICA;

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            int yPosition = 700; // Vị trí bắt đầu từ trên xuống

            // Tiêu đề - CENTER
            String title = "HỒ SƠ BỆNH ÁN";
            contentStream.setFont(fontBold, 16);
            float titleWidth = fontBold.getStringWidth(title) / 1000 * 16;
            float titleX = (page.getMediaBox().getWidth() - titleWidth) / 2;

            contentStream.beginText();
            contentStream.newLineAtOffset(titleX, yPosition);
            contentStream.showText(title);
            contentStream.endText();

            yPosition -= 30;

            // Thông tin cơ bản - LEFT ALIGN
            contentStream.setFont(fontNormal, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Mã hồ sơ: " + medicalRecord.getRecordId());
            contentStream.endText();

            yPosition -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Ngày khám: " + dateFormat.format(medicalRecord.getCreatedAt()));
            contentStream.endText();

            yPosition -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Bệnh nhân: " + safeString(patient.getFullName()));
            contentStream.endText();

            yPosition -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Bác sĩ: " + safeString(doctor.getFullName()));
            contentStream.endText();

            yPosition -= 30;

            // Triệu chứng
            contentStream.setFont(fontBold, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("TRIỆU CHỨNG:");
            contentStream.endText();

            yPosition -= 20;
            contentStream.setFont(fontNormal, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(safeString(medicalRecord.getSymptoms()));
            contentStream.endText();

            yPosition -= 30;

            // Chẩn đoán
            contentStream.setFont(fontBold, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("CHẨN ĐOÁN:");
            contentStream.endText();

            yPosition -= 20;
            contentStream.setFont(fontNormal, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(safeString(medicalRecord.getDiagnosis()));
            contentStream.endText();

            yPosition -= 30;

            // Điều trị
            contentStream.setFont(fontBold, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("ĐIỀU TRỊ:");
            contentStream.endText();

            yPosition -= 20;
            contentStream.setFont(fontNormal, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(safeString(medicalRecord.getTreatment()));
            contentStream.endText();

            yPosition -= 30;

            // Ghi chú (nếu có)
            if (medicalRecord.getNotes() != null && !medicalRecord.getNotes().trim().isEmpty()) {
                contentStream.setFont(fontBold, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("GHI CHÚ:");
                contentStream.endText();

                yPosition -= 20;
                contentStream.setFont(fontNormal, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(safeString(medicalRecord.getNotes()));
                contentStream.endText();

                yPosition -= 30;
            }

            // Đơn thuốc
            if (prescriptions != null && !prescriptions.isEmpty()) {
                contentStream.setFont(fontBold, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("ĐƠN THUỐC");
                contentStream.endText();

                yPosition -= 30;

                for (int i = 0; i < prescriptions.size(); i++) {
                    Prescriptions p = prescriptions.get(i);

                    // Tên thuốc
                    contentStream.setFont(fontBold, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText((i + 1) + ". " + safeString(p.getMedicationName()));
                    contentStream.endText();

                    yPosition -= 20;

                    // Thông tin thuốc
                    contentStream.setFont(fontNormal, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(70, yPosition);
                    contentStream.showText("Liều dùng: " + safeString(p.getDosage()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(200, yPosition);
                    contentStream.showText("Tần suất: " + safeString(p.getFrequency()));
                    contentStream.endText();

                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.newLineAtOffset(70, yPosition);
                    contentStream.showText("Thời gian: " + safeString(p.getDuration()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(200, yPosition);
                    contentStream.showText("Ghi chú: " + safeString(p.getNotes()));
                    contentStream.endText();

                    yPosition -= 25;

                    // Ngắt trang nếu hết chỗ
                    if (yPosition < 50) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = 700;
                    }
                }
            }

            contentStream.close();
            document.save(outputStream);

            byte[] pdfBytes = outputStream.toByteArray();
            System.out.println("=== PDFBox PDF ĐÃ TẠO THÀNH CÔNG ===");
            System.out.println("PDF size: " + pdfBytes.length + " bytes");
            return pdfBytes;

        } catch (Exception e) {
            System.err.println("Lỗi PDFBox: " + e.getMessage());
            throw new RuntimeException("Error generating PDF with PDFBox: " + e.getMessage(), e);
        }
    }

    private String safeString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Không có thông tin";
        }
        return value;
    }
}
