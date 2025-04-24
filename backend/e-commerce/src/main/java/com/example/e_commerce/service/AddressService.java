package com.example.e_commerce.service;


import com.example.e_commerce.dto.AddressDTO;
import java.util.List;

public interface AddressService {
    AddressDTO create(AddressDTO dto);
    AddressDTO getById(Integer id);
    List<AddressDTO> getAllByUser(Integer userId);
    AddressDTO update(Integer id, AddressDTO dto);
    void delete(Integer id);
}
