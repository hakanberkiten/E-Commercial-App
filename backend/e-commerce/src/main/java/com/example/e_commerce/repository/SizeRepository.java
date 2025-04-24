package com.example.e_commerce.repository;


import com.example.e_commerce.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {

    /**
     * İsimle arama yapmak isterseniz
     */
    Optional<Size> findByName(String name);
}
