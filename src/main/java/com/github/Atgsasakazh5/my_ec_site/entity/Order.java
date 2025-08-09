package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Order {

    private Long id;
    private Long userId; // 外部キー
    private String status;
    private Integer totalPrice;
    private String shippingAddress;
    private String postalCode;
    private String shippingName;
    private LocalDateTime orderedAt;

}
