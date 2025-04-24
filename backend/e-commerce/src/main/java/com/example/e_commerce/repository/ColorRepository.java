package com.example.e_commerce.repository;


import com.example.e_commerce.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {

    /**
     * İsimle arama yapmak isterseniz
     */
    Optional<Color> findByName(String name);
}
