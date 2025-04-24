package com.example.e_commerce.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "color")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ColorID")
    private Integer id;

    @Column(name = "ColorName", length = 50, nullable = false)
    private String name;
}

