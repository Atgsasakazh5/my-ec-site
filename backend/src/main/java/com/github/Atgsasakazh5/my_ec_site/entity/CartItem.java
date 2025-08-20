package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class CartItem {

    private Long id;
    private Long cartId; // 外部キー
    private Long skuId; // 外部キー
    private Integer quantity; // 数量

}
