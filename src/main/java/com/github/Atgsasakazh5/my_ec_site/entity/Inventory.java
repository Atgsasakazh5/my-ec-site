package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Inventory {

    private Long id;
    private Long skuId; // 外部キー
    private Integer quantity;
    private LocalDateTime updatedAt;
}
