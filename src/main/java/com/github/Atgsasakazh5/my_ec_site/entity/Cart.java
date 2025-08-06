package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Cart {
    private Long id;
    private Long userId; // 外部キー
}
