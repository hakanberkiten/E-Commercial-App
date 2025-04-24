package com.example.e_commerce.service.impl;


import com.example.e_commerce.dto.CardInfoDTO;
import com.example.e_commerce.entity.CardInfo;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CardInfoRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.CardInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardInfoServiceImpl implements CardInfoService {

    private final CardInfoRepository repo;
    private final UserRepository userRepo;

    private CardInfoDTO toDto(CardInfo e) {
        CardInfoDTO dto = new CardInfoDTO();
        dto.setId(e.getId());
        dto.setUserId(e.getUser().getId());
        dto.setCardHolderName(e.getCardHolderName());
        dto.setCardType(e.getCardType());
        dto.setCardNumber(e.getCardNumber());
        dto.setExpirationMonth(e.getExpirationMonth());
        dto.setExpirationYear(e.getExpirationYear());
        dto.setCvv(e.getCvv());
        return dto;
    }

    private CardInfo toEntity(CardInfoDTO dto) {
        CardInfo e = new CardInfo();
        User u = userRepo.findById(dto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User","id",dto.getUserId()));
        e.setUser(u);
        e.setCardHolderName(dto.getCardHolderName());
        e.setCardType(dto.getCardType());
        e.setCardNumber(dto.getCardNumber());
        e.setExpirationMonth(dto.getExpirationMonth().byteValue());
        e.setExpirationYear(dto.getExpirationYear().shortValue());
        e.setCvv(dto.getCvv());
        return e;
    }

    @Override
    public CardInfoDTO create(CardInfoDTO dto) {
        CardInfo saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public CardInfoDTO getById(Integer id) {
        return repo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("CardInfo","id",id));
    }

    @Override
    public List<CardInfoDTO> getAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public CardInfoDTO update(Integer id, CardInfoDTO dto) {
        CardInfo existing = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CardInfo","id",id));
        // sadece değiştirilmek istenen alanları setleyelim
        existing.setCardHolderName(dto.getCardHolderName());
        existing.setCardType(dto.getCardType());
        existing.setCardNumber(dto.getCardNumber());
        existing.setExpirationMonth(dto.getExpirationMonth().byteValue());
        existing.setExpirationYear(dto.getExpirationYear().shortValue());
        existing.setCvv(dto.getCvv());
        CardInfo updated = repo.save(existing);
        return toDto(updated);
    }

    @Override
    public void delete(Integer id) {
        CardInfo e = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CardInfo","id",id));
        repo.delete(e);
    }
}
