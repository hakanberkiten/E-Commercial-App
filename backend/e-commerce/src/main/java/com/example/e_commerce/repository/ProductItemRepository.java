package com.example.e_commerce.repository;


import com.example.e_commerce.entity.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, Integer> {

    /**
     * Bir ürüne ait varyantları getirir
     */
    List<ProductItem> findByProductId(Integer productId);

    /**
     * Belirli renge ait varyantları getirir
     */
    List<ProductItem> findByColorId(Integer colorId);

    /**
     * Belirli bedene ait varyantları getirir
     */
    List<ProductItem> findBySizeId(Integer sizeId);
}
