package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Sku {
    private Long id;
    private Long productId;
    private String size;
    private String color;
    private Integer extraPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
