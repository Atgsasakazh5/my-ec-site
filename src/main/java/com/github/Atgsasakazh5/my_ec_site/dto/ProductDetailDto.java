package com.github.Atgsasakazh5.my_ec_site.dto;

import java.util.List;

public record ProductDetailDto(Long id,
                               String name,
                               String description,
                               String imageUrl,
                               CategoryDto category,
                               List<SkuDto> skus) {
}
