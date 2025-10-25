package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.entity.Prescriptions;
import com.spring.bioMedical.entity.Users;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class SimplePDFBoxReportService {

    public byte[] generateSimpleMedicalRecordReport(MedicalRecords medicalRecord,
            Users patient, Users doctor, List<Prescriptions> prescriptions) {
        
        System.out.println("=== TẠO PDF ĐƠN GIẢN VỚI PDFBox ===");
        
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage();
            document.addPage(page);
            
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Chỉ viết một dòng đơn giản để test            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("HỒ SƠ BỆNH ÁN - TEST THÀNH CÔNG");
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Mã hồ sơ: " + medicalRecord.getRecordId());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Bệnh nhân: " + safeString(patient.getFullName()));
            contentStream.endText();
            
            contentStream.close();
            document.save(outputStream);
            
            byte[] pdfBytes = outputStream.toByteArray();
            System.out.println("=== PDF ĐƠN GIẢN ĐÃ TẠO THÀNH CÔNG ===");
            System.out.println("PDF size: " + pdfBytes.length + " bytes");
            return pdfBytes;
            
        } catch (Exception e) {
            System.err.println("Lỗi PDFBox đơn giản: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Simple PDF Error: " + e.getMessage(), e);
        }
    }
    
    private String safeString(String value) {
        return value != null ? value : "Không xác định";
    }
}