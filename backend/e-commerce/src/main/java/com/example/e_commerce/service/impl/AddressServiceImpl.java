package com.example.e_commerce.service.impl;


import com.example.e_commerce.dto.AddressDTO;
import com.example.e_commerce.entity.Address;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.AddressRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepo;
    private final UserRepository userRepo;

    private AddressDTO toDto(Address e) {
        AddressDTO dto = new AddressDTO();
        dto.setId(e.getId());
        dto.setUserId(e.getUser().getId());
        dto.setAddressLine(e.getAddressLine());
        dto.setCity(e.getCity());
        dto.setState(e.getState());
        dto.setPostalCode(e.getPostalCode());
        dto.setCountry(e.getCountry());
        dto.setAddressType(e.getAddressType());
        return dto;
    }

    private Address toEntity(AddressDTO dto) {
        User user = userRepo.findById(dto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User","id",dto.getUserId()));
        Address e = new Address();
        e.setUser(user);
        e.setAddressLine(dto.getAddressLine());
        e.setCity(dto.getCity());
        e.setState(dto.getState());
        e.setPostalCode(dto.getPostalCode());
        e.setCountry(dto.getCountry());
        e.setAddressType(dto.getAddressType());
        return e;
    }

    @Override
    public AddressDTO create(AddressDTO dto) {
        Address saved = addressRepo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public AddressDTO getById(Integer id) {
        return addressRepo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Address","id",id));
    }

    @Override
    public List<AddressDTO> getAllByUser(Integer userId) {
        return addressRepo.findByUserId(userId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public AddressDTO update(Integer id, AddressDTO dto) {
        Address existing = addressRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Address","id",id));
        // Sadece değişen alanları setleyelim
        existing.setAddressLine(dto.getAddressLine());
        existing.setCity(dto.getCity());
        existing.setState(dto.getState());
        existing.setPostalCode(dto.getPostalCode());
        existing.setCountry(dto.getCountry());
        existing.setAddressType(dto.getAddressType());
        Address updated = addressRepo.save(existing);
        return toDto(updated);
    }

    @Override
    public void delete(Integer id) {
        Address e = addressRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Address","id",id));
        addressRepo.delete(e);
    }
}
