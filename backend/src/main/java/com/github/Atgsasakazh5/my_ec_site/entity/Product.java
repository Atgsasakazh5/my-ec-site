package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Product {

    private Long id;
    private String name;
    private Integer price;
    private String description;
    private String imageUrl;
    private Integer categoryId; // 外部キー
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
