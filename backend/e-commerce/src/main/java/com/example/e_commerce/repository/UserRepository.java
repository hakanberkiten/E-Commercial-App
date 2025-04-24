package com.example.e_commerce.repository;


import com.example.e_commerce.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
    // İleride findByUsername gibi metotlar ekleyebilirsin

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
