package com.spring.bioMedical.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.entity.Prescriptions;
import com.spring.bioMedical.entity.Users;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ITextReportService {

    private PdfFont font;
    private PdfFont fontBold;

    private void loadFonts() {
        try {
            String fontPath = "src/main/resources/fonts/Arial.otf";
            String fontPathBold = "src/main/resources/fonts/ArialBold.otf";
            font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);
            fontBold = PdfFontFactory.createFont(fontPathBold, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (IOException e) {
            throw new RuntimeException("Không thể load font Unicode", e);
        }
    }

    public byte[] generateMedicalRecordReport(MedicalRecords medicalRecord,
                                              Users patient,
                                              Users doctor,
                                              List<Prescriptions> prescriptions) {

        loadFonts();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            // Tiêu đề trung tâm
            document.add(new Paragraph("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM")
                    .setFont(fontBold).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Độc lập - Tự do - Hạnh phúc")
                    .setFont(font).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("HỒ SƠ BỆNH ÁN")
                    .setFont(fontBold).setFontSize(18).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // Thông tin hành chính
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{3, 7}));
            infoTable.setWidth(UnitValue.createPercentValue(100));

            infoTable.addCell(new Cell().add(new Paragraph("Mã hồ sơ:").setFont(fontBold)));
            infoTable.addCell(new Cell().add(new Paragraph(String.valueOf(medicalRecord.getRecordId())).setFont(font)));

            infoTable.addCell(new Cell().add(new Paragraph("Ngày khám:").setFont(fontBold)));
            infoTable.addCell(new Cell().add(new Paragraph(dateFormat.format(medicalRecord.getCreatedAt())).setFont(font)));

            infoTable.addCell(new Cell().add(new Paragraph("Họ tên bệnh nhân:").setFont(fontBold)));
            infoTable.addCell(new Cell().add(new Paragraph(safeString(patient.getFullName())).setFont(font)));

            infoTable.addCell(new Cell().add(new Paragraph("Điện thoại:").setFont(fontBold)));
            infoTable.addCell(new Cell().add(new Paragraph(safeString(patient.getPhone())).setFont(font)));

            infoTable.addCell(new Cell().add(new Paragraph("Email:").setFont(fontBold)));
            infoTable.addCell(new Cell().add(new Paragraph(safeString(patient.getEmail())).setFont(font)));

            infoTable.addCell(new Cell().add(new Paragraph("Bác sĩ khám:").setFont(fontBold)));
            infoTable.addCell(new Cell().add(new Paragraph(safeString(doctor.getFullName())).setFont(font)));

            infoTable.addCell(new Cell().add(new Paragraph("Cơ sở y tế:").setFont(fontBold)));
            infoTable.addCell(new Cell().add(new Paragraph("Phòng khám Đa khoa").setFont(font)));

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Phần chẩn đoán
            document.add(new Paragraph("I. CHẨN ĐOÁN VÀ ĐIỀU TRỊ").setFont(fontBold).setFontSize(14));
            document.add(new Paragraph("Triệu chứng: " + safeString(medicalRecord.getSymptoms())).setFont(font));
            document.add(new Paragraph("Chẩn đoán: " + safeString(medicalRecord.getDiagnosis())).setFont(font));
            document.add(new Paragraph("Phương pháp điều trị: " + safeString(medicalRecord.getTreatment())).setFont(font));
            if (medicalRecord.getNotes() != null && !medicalRecord.getNotes().trim().isEmpty()) {
                document.add(new Paragraph("Ghi chú: " + safeString(medicalRecord.getNotes())).setFont(font));
            }
            document.add(new Paragraph("\n"));

            // Đơn thuốc
            if (prescriptions != null && !prescriptions.isEmpty()) {
                document.add(new Paragraph("II. ĐƠN THUỐC").setFont(fontBold).setFontSize(14));

                Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 3}));
                table.setWidth(UnitValue.createPercentValue(100));

                table.addHeaderCell(new Cell().add(new Paragraph("Tên thuốc").setFont(fontBold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Liều dùng").setFont(fontBold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Tần suất").setFont(fontBold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Thời gian").setFont(fontBold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Ghi chú").setFont(fontBold)));

                for (Prescriptions p : prescriptions) {
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getMedicationName())).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getDosage())).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getFrequency())).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getDuration())).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getNotes())).setFont(font)));
                }

                document.add(table);
            }

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("III. KÝ XÁC NHẬN").setFont(fontBold).setFontSize(14));
            document.add(new Paragraph("Bác sĩ: " + safeString(doctor.getFullName())).setFont(font).setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("Ngày ký: " + dateFormat.format(medicalRecord.getCreatedAt())).setFont(font).setTextAlignment(TextAlignment.RIGHT));

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF with iText: " + e.getMessage(), e);
        }
    }

    private String safeString(String value) {
        return (value == null || value.trim().isEmpty()) ? "Không có thông tin" : value;
    }
}
