package com.example.e_commerce.repository;


import com.example.e_commerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    /**
     * Belirli bir kullanıcıya ait adresleri getirir
     */
    List<Address> findByUserId(Integer userId);
}
