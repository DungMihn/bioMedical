package com.spring.bioMedical.service;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.entity.Prescriptions;
import com.spring.bioMedical.entity.Users;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;



@Service
public class ReportService {

   public byte[] generateMedicalRecordReport(MedicalRecords medicalRecord,
            Users patient, Users doctor, List<Prescriptions> prescriptions) {
        
        System.out.println("=== TẠO PDF VỚI iText 7 ===");
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Sử dụng font chuẩn - API ĐÚNG
            PdfFont font;
            PdfFont fontBold;
            
            try {
                // Thử sử dụng font có sẵn
                font = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
                fontBold = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            } catch (Exception e) {
                // Fallback đến font mặc định
                font = PdfFontFactory.createFont();
                fontBold = PdfFontFactory.createFont();
            }
            
            // Tiêu đề
            Paragraph title = new Paragraph("HỒ SƠ BỆNH ÁN")
                    .setFont(fontBold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            // Thông tin cơ bản
            document.add(new Paragraph("Mã hồ sơ: " + medicalRecord.getRecordId()).setFont(font));
            document.add(new Paragraph("Ngày khám: " + dateFormat.format(medicalRecord.getCreatedAt())).setFont(font));
            document.add(new Paragraph(" "));
            
            // Thông tin bệnh nhân
            Paragraph patientInfo = new Paragraph("THÔNG TIN BỆNH NHÂN")
                    .setFont(fontBold)
                    .setFontSize(14);
            document.add(patientInfo);
            
            document.add(new Paragraph("Họ tên: " + safeString(patient.getFullName())).setFont(font));
            document.add(new Paragraph("Điện thoại: " + safeString(patient.getPhone())).setFont(font));
            document.add(new Paragraph("Email: " + safeString(patient.getEmail())).setFont(font));
            document.add(new Paragraph(" "));
            
            // Thông tin bác sĩ
            Paragraph doctorInfo = new Paragraph("THÔNG TIN BÁC SĨ")
                    .setFont(fontBold)
                    .setFontSize(14);
            document.add(doctorInfo);
            
            document.add(new Paragraph("Bác sĩ: " + safeString(doctor.getFullName())).setFont(font));
            document.add(new Paragraph("Cơ sở: Phòng khám Đa khoa").setFont(font));
            document.add(new Paragraph(" "));
            
            // Chẩn đoán và điều trị
            Paragraph diagnosisTitle = new Paragraph("CHẨN ĐOÁN VÀ ĐIỀU TRỊ")
                    .setFont(fontBold)
                    .setFontSize(14);
            document.add(diagnosisTitle);
            
            document.add(new Paragraph("Triệu chứng: " + safeString(medicalRecord.getSymptoms())).setFont(font));
            document.add(new Paragraph("Chẩn đoán: " + safeString(medicalRecord.getDiagnosis())).setFont(font));
            document.add(new Paragraph("Điều trị: " + safeString(medicalRecord.getTreatment())).setFont(font));
            
            if (medicalRecord.getNotes() != null && !medicalRecord.getNotes().trim().isEmpty()) {
                document.add(new Paragraph("Ghi chú: " + safeString(medicalRecord.getNotes())).setFont(font));
            }
            
            document.add(new Paragraph(" "));
            
            // Đơn thuốc
            if (prescriptions != null && !prescriptions.isEmpty()) {
                Paragraph medsTitle = new Paragraph("ĐƠN THUỐC")
                        .setFont(fontBold)
                        .setFontSize(14);
                document.add(medsTitle);
                
                // Tạo bảng đơn thuốc
                Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 3}));
                table.setWidth(UnitValue.createPercentValue(100));
                
                // Header
                table.addHeaderCell(new Cell().add(new Paragraph("Thuốc").setFont(fontBold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Liều dùng").setFont(fontBold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Tần suất").setFont(fontBold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Thời gian").setFont(fontBold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Ghi chú").setFont(fontBold)));
                
                // Data
                for (Prescriptions p : prescriptions) {
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getMedicationName())).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getDosage())).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getFrequency())).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getDuration())).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph(safeString(p.getNotes())).setFont(font)));
                }
                
                document.add(table);
            }
            
            document.close();
            
            byte[] pdfBytes = outputStream.toByteArray();
            System.out.println("=== iText PDF ĐÃ TẠO THÀNH CÔNG ===");
            System.out.println("PDF size: " + pdfBytes.length + " bytes");
            return pdfBytes;
            
        } catch (Exception e) {
            System.err.println("Lỗi iText: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF with iText: " + e.getMessage(), e);
        }
    }
    
    private String safeString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Không có thông tin";
        }
        return value;
    }
}
