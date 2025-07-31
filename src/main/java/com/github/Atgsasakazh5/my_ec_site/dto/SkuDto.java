package com.github.Atgsasakazh5.my_ec_site.dto;

public record SkuDto(Long id,
                     String size,
                     String color,
                     Integer extraPrice,
                     InventoryDto inventory) {
}
