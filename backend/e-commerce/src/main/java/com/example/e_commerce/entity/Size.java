package com.example.e_commerce.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "size")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SizeID")
    private Integer id;

    @Column(name = "SizeName", length = 50, nullable = false)
    private String name;
}
