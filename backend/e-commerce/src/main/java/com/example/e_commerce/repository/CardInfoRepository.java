package com.example.e_commerce.repository;


import com.example.e_commerce.entity.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Integer> {

    /**
     * Belirli bir kullanıcıya ait kart bilgilerini döner.
     */
    List<CardInfo> findByUserId(Integer userId);
}
