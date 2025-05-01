package com.example.e_commerce.repository;

import com.example.e_commerce.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName OR u.role.roleName = CONCAT('ROLE_', :roleName)")
    List<User> findByRoleName(@Param("roleName") String roleName);
}
