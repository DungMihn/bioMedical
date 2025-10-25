package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.entity.Prescriptions;
import com.spring.bioMedical.entity.Users;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JasperReportService {

    public String generateAndSaveMedicalRecordReport(MedicalRecords medicalRecord,
                                                   Users patient, 
                                                   Users doctor, 
                                                   List<Prescriptions> prescriptions) throws JRException {
        
        System.out.println("=== TẠO BÁO CÁO VỚI JASPERREPORTS ===");
        
        // Tạo JasperDesign
        JasperDesign jasperDesign = createJasperDesign(medicalRecord, patient, doctor);
        
        // Tạo datasource từ prescriptions
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(prescriptions);
        
        // Biên dịch và điền báo cáo
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        Map<String, Object> parameters = createParameters(medicalRecord, patient, doctor);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        
        // Lưu file PDF
        String filePath = "medical_record_" + medicalRecord.getRecordId() + ".pdf";
        JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);
        
        return filePath;
    }
    
   public JasperDesign createJasperDesign(MedicalRecords medicalRecord, Users patient, Users doctor) throws JRException {
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("MedicalRecordReport");
        jasperDesign.setPageWidth(595);
        jasperDesign.setPageHeight(842);
        jasperDesign.setColumnWidth(515);
        jasperDesign.setLeftMargin(40);
        jasperDesign.setRightMargin(40);
        jasperDesign.setTopMargin(50);
        jasperDesign.setBottomMargin(50);
        
        // Thêm các field
        addFields(jasperDesign);
        
        // Tạo các band
        jasperDesign.setTitle(createTitleBand(medicalRecord, patient, doctor));
        jasperDesign.setColumnHeader(createColumnHeaderBand());
        
        JRDesignSection detailSection = (JRDesignSection) jasperDesign.getDetailSection();
        detailSection.addBand(createDetailBand());
        
        jasperDesign.setSummary(createSummaryBand(medicalRecord));
        
        return jasperDesign;
    }
    
    private void addFields(JasperDesign jasperDesign) throws JRException {
        String[] fieldNames = {"medicationName", "dosage", "frequency", "duration", "notes"};
        
        for (String fieldName : fieldNames) {
            JRDesignField field = new JRDesignField();
            field.setName(fieldName);
            field.setValueClass(String.class);
            jasperDesign.addField(field);
        }
    }
    
    private JRDesignBand createTitleBand(MedicalRecords medicalRecord, Users patient, Users doctor) {
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(120);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        // Tiêu đề chính
        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setText("HỒ SƠ BỆNH ÁN");
        titleText.setX(10);
        titleText.setY(10);
        titleText.setWidth(500);
        titleText.setHeight(25);
        titleText.setFontName("Arial");
        titleText.setBold(true);
        titleText.setFontSize(18f);
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleBand.addElement(titleText);
        
        // Thông tin hồ sơ
        JRDesignStaticText recordInfo = new JRDesignStaticText();
        recordInfo.setText("Mã hồ sơ: " + medicalRecord.getRecordId() + " | Ngày khám: " + dateFormat.format(medicalRecord.getCreatedAt()));
        recordInfo.setX(10);
        recordInfo.setY(40);
        recordInfo.setWidth(500);
        recordInfo.setHeight(20);
        recordInfo.setFontName("Arial");
        recordInfo.setFontSize(12f);
        recordInfo.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleBand.addElement(recordInfo);
        
        // Thông tin bệnh nhân
        JRDesignStaticText patientInfo = new JRDesignStaticText();
        patientInfo.setText("Bệnh nhân: " + safeString(patient.getFullName()) + " | Điện thoại: " + safeString(patient.getPhone()));
        patientInfo.setX(10);
        patientInfo.setY(65);
        patientInfo.setWidth(500);
        patientInfo.setHeight(20);
        patientInfo.setFontName("Arial");
        patientInfo.setFontSize(12f);
        patientInfo.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        titleBand.addElement(patientInfo);
        
        // Thông tin bác sĩ
        JRDesignStaticText doctorInfo = new JRDesignStaticText();
        doctorInfo.setText("Bác sĩ: " + safeString(doctor.getFullName()) + " | Cơ sở: Phòng khám Đa khoa");
        doctorInfo.setX(10);
        doctorInfo.setY(85);
        doctorInfo.setWidth(500);
        doctorInfo.setHeight(20);
        doctorInfo.setFontName("Arial");
        doctorInfo.setFontSize(12f);
        doctorInfo.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        titleBand.addElement(doctorInfo);
        
        return titleBand;
    }
    
    private JRDesignBand createColumnHeaderBand() {
        JRDesignBand columnHeaderBand = new JRDesignBand();
        columnHeaderBand.setHeight(30);
        
        String[] headers = {"Thuốc", "Liều dùng", "Tần suất", "Thời gian", "Ghi chú"};
        int[] widths = {150, 80, 80, 80, 125};
        int[] positions = {10, 160, 240, 320, 400};
        
        for (int i = 0; i < headers.length; i++) {
            JRDesignStaticText header = new JRDesignStaticText();
            header.setText(headers[i]);
            header.setX(positions[i]);
            header.setY(5);
            header.setWidth(widths[i]);
            header.setHeight(20);
            header.setFontName("Arial");
            header.setBold(true);
            header.setFontSize(12f);
            header.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            header.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            header.setBackcolor(Color.LIGHT_GRAY);
            columnHeaderBand.addElement(header);
        }
        
        return columnHeaderBand;
    }
    
    private JRDesignBand createDetailBand() {
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(25);
        
        String[] fieldNames = {"medicationName", "dosage", "frequency", "duration", "notes"};
        int[] widths = {150, 80, 80, 80, 125};
        int[] positions = {10, 160, 240, 320, 400};
        
        for (int i = 0; i < fieldNames.length; i++) {
            JRDesignTextField textField = new JRDesignTextField();
            textField.setX(positions[i]);
            textField.setY(2);
            textField.setWidth(widths[i]);
            textField.setHeight(20);
            textField.setExpression(new JRDesignExpression("$F{" + fieldNames[i] + "}"));
            textField.setFontName("Arial");
            textField.setFontSize(10f);
            textField.setStretchWithOverflow(true);
            
            if (i == 0) {
                textField.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
            } else if (i == fieldNames.length - 1) {
                textField.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
            } else {
                textField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            }
            
            detailBand.addElement(textField);
        }
        
        // Đường kẻ ngăn cách
        JRDesignLine line = new JRDesignLine();
        line.setX(10);
        line.setY(24);
        line.setWidth(515);
        line.setHeight(1);
        detailBand.addElement(line);
        
        return detailBand;
    }
    
    public JRDesignBand createSummaryBand(MedicalRecords medicalRecord) {
        JRDesignBand summaryBand = new JRDesignBand();
        summaryBand.setHeight(100);
        
        // Thông tin chẩn đoán
        JRDesignStaticText diagnosisTitle = new JRDesignStaticText();
        diagnosisTitle.setText("CHẨN ĐOÁN VÀ ĐIỀU TRỊ");
        diagnosisTitle.setX(10);
        diagnosisTitle.setY(10);
        diagnosisTitle.setWidth(500);
        diagnosisTitle.setHeight(20);
        diagnosisTitle.setFontName("Arial");
        diagnosisTitle.setBold(true);
        diagnosisTitle.setFontSize(14f);
        summaryBand.addElement(diagnosisTitle);
        
        // Triệu chứng
        JRDesignTextField symptomsField = new JRDesignTextField();
        symptomsField.setX(10);
        symptomsField.setY(35);
        symptomsField.setWidth(500);
        symptomsField.setHeight(15);
        symptomsField.setExpression(new JRDesignExpression("\"Triệu chứng: \" + ($P{symptoms} != null ? $P{symptoms} : \"Không có thông tin\")"));
        symptomsField.setFontName("Arial");
        symptomsField.setFontSize(11f);
        summaryBand.addElement(symptomsField);
        
        // Chẩn đoán
        JRDesignTextField diagnosisField = new JRDesignTextField();
        diagnosisField.setX(10);
        diagnosisField.setY(50);
        diagnosisField.setWidth(500);
        diagnosisField.setHeight(15);
        diagnosisField.setExpression(new JRDesignExpression("\"Chẩn đoán: \" + ($P{diagnosis} != null ? $P{diagnosis} : \"Không có thông tin\")"));
        diagnosisField.setFontName("Arial");
        diagnosisField.setFontSize(11f);
        summaryBand.addElement(diagnosisField);
        
        // Điều trị
        JRDesignTextField treatmentField = new JRDesignTextField();
        treatmentField.setX(10);
        treatmentField.setY(65);
        treatmentField.setWidth(500);
        treatmentField.setHeight(15);
        treatmentField.setExpression(new JRDesignExpression("\"Điều trị: \" + ($P{treatment} != null ? $P{treatment} : \"Không có thông tin\")"));
        treatmentField.setFontName("Arial");
        treatmentField.setFontSize(11f);
        summaryBand.addElement(treatmentField);
        
        return summaryBand;
    }
    
    public Map<String, Object> createParameters(MedicalRecords medicalRecord, Users patient, Users doctor) {
        Map<String, Object> parameters = new HashMap<>();
        
        parameters.put("symptoms", medicalRecord.getSymptoms());
        parameters.put("diagnosis", medicalRecord.getDiagnosis());
        parameters.put("treatment", medicalRecord.getTreatment());
        parameters.put("notes", medicalRecord.getNotes());
        
        return parameters;
    }
    
    private String safeString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Không có thông tin";
        }
        return value;
    }
}