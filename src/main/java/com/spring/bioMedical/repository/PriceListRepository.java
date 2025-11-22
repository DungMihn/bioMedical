package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Clinics;
import com.spring.bioMedical.entity.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {

// Lấy danh sách theo loại (SERVICE / MEDICINE) để admin cấu hình, hiển thị
    List<PriceList> findByItemTypeOrderByItemNameAsc(String itemType);

    // Tìm 1 item theo loại + tên (không phân biệt hoa thường)
    Optional<PriceList> findFirstByItemTypeAndItemNameIgnoreCase(String itemType, String itemName);

}
