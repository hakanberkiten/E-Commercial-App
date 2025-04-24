package com.example.e_commerce.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cardinfo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CardInfoID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(length = 50)
    private String cardHolderName;

    @Column(length = 50)
    private String cardType;

    @Column(length = 50)
    private String cardNumber;

    private Byte expirationMonth;  // tinyint

    private Short expirationYear;  // smallint

    private Integer cvv;
}
