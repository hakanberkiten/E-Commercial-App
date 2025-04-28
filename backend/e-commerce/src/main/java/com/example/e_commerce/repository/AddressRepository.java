// AddressRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
