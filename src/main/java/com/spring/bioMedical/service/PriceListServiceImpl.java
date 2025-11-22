package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.PriceList;
import com.spring.bioMedical.repository.PriceListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PriceListServiceImpl implements PriceListService {

    @Autowired
    private PriceListRepository priceListRepository;

    @Override
    public List<PriceList> findAll() {
        return priceListRepository.findAll();
    }

    @Override
    public List<PriceList> findAllActive() {
//        try {
//            return priceListRepository.findByActiveTrueOrderByItemNameAsc();
//        } catch (Exception e) {
//            // nếu Entity chưa có field active thì tạm trả về tất cả
//            return priceListRepository.findAll();
//        }
        return null;
    }

    @Override
    public Optional<PriceList> findById(Long id) {
        return priceListRepository.findById(id);
    }

    @Override
    public PriceList save(PriceList priceList) {
        return priceListRepository.save(priceList);
    }

    @Override
    public void deleteById(Long id) {
        priceListRepository.deleteById(id);
    }

    @Override
    public List<PriceList> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllActive();
        }
//        return priceListRepository.findByItemNameContainingIgnoreCase(keyword.trim());
        return null;
    }
}
