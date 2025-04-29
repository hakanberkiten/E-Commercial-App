// AddressController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.dto.AddressDTO;
import com.example.e_commerce.entity.Address;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.UserAddress;
import com.example.e_commerce.service.AddressService;
import com.example.e_commerce.service.UserAddressService;
import com.example.e_commerce.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// AddressController sınıfını düzenleyin
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserAddressService userAddressService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> addAddress(@RequestBody Map<String, Object> payload) {
        try {
            // Adres ve kullanıcı ilişkisini kur
            Address address = new Address();
            address.setStreet((String) payload.get("street"));
            address.setCity((String) payload.get("city"));
            address.setState((String) payload.get("state"));
            address.setPincode((String) payload.get("zipCode"));
            address.setCountry((String) payload.get("country"));
            address.setBuildingName((String) payload.get("buildingName"));

            Address savedAddress = addressService.saveAddress(address);
            
            // UserAddress ilişkisini oluştur
            UserAddress userAddress = new UserAddress();
            userAddress.setAddress(savedAddress);
            
            User user = userService.getUserById(Long.valueOf((String) payload.get("userId")));
            userAddress.setUser(user);
            
            // Eğer ilk adres ise veya isDefault=true ise varsayılan yap
            Boolean isDefault = (Boolean) payload.getOrDefault("isDefault", false);
            userAddress.setIsDefault(isDefault);
            
            userAddressService.saveUserAddress(userAddress);
            
            // DTO olarak döndür
            AddressDTO responseDto = AddressDTO.builder()
                    .id(savedAddress.getAddressId())
                    .street(savedAddress.getStreet())
                    .city(savedAddress.getCity())
                    .state(savedAddress.getState())
                    .zipCode(savedAddress.getPincode())
                    .country(savedAddress.getCountry())
                    .isDefault(isDefault)
                    .build();
                    
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            Address address = addressService.getAddressById(id);
            
            if (payload.containsKey("street")) address.setStreet((String) payload.get("street"));
            if (payload.containsKey("city")) address.setCity((String) payload.get("city"));
            if (payload.containsKey("state")) address.setState((String) payload.get("state"));
            if (payload.containsKey("zipCode")) address.setPincode((String) payload.get("zipCode"));
            if (payload.containsKey("country")) address.setCountry((String) payload.get("country"));
            if (payload.containsKey("buildingName")) address.setBuildingName((String) payload.get("buildingName")); // Bu satır eksik

            Address updatedAddress = addressService.saveAddress(address);
            
            // isDefault değişti mi kontrol et
            if (payload.containsKey("isDefault") && (Boolean) payload.get("isDefault")) {
                Long userId = Long.valueOf((String) payload.get("userId"));
                userService.setDefaultAddress(userId, id);
            }
            
            // DTO olarak döndür
            AddressDTO responseDto = AddressDTO.builder()
                    .id(updatedAddress.getAddressId())
                    .street(updatedAddress.getStreet())
                    .city(updatedAddress.getCity())
                    .state(updatedAddress.getState())
                    .zipCode(updatedAddress.getPincode())
                    .country(updatedAddress.getCountry())
                    .buildingName(updatedAddress.getBuildingName())
                    .isDefault((Boolean) payload.getOrDefault("isDefault", false))
                    .build();
                    
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        try {
            // Adres-kullanıcı ilişkisini sil
            userAddressService.deleteUserAddress(id);
            
            // Adresi sil
            addressService.deleteAddress(id);
            
            return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddress(@PathVariable Long id) {
        try {
            Address address = addressService.getAddressById(id);
            
            AddressDTO responseDto = AddressDTO.builder()
                    .id(address.getAddressId())
                    .street(address.getStreet())
                    .city(address.getCity())
                    .state(address.getState())
                    .zipCode(address.getPincode())
                    .country(address.getCountry())
                    .buildingName(address.getBuildingName())
                    .build();
                    
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}