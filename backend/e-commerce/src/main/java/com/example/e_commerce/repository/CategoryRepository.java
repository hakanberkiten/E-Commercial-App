package com.example.e_commerce.repository;


import com.example.e_commerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    /**
     * İsteğe bağlı: kategori adına göre arama yapmak isterseniz
     */
    Optional<Category> findByName(String name);
}
