package com.example.e_commerce.service;


import com.example.e_commerce.dto.CardInfoDTO;
import java.util.List;

public interface CardInfoService {
    CardInfoDTO create(CardInfoDTO dto);
    CardInfoDTO getById(Integer id);
    List<CardInfoDTO> getAll();
    CardInfoDTO update(Integer id, CardInfoDTO dto);
    void delete(Integer id);
}
