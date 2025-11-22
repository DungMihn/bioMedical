package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.PriceList;

import java.util.List;
import java.util.Optional;

public interface PriceListService {

    List<PriceList> findAll();

    List<PriceList> findAllActive();

    Optional<PriceList> findById(Long id);

    PriceList save(PriceList priceList);

    void deleteById(Long id);

    List<PriceList> searchByName(String keyword);
}
