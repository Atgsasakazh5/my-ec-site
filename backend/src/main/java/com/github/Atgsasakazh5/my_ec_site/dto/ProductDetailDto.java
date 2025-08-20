package com.github.Atgsasakazh5.my_ec_site.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailDto(Long id,
                               String name,
                               Integer price,
                               String description,
                               String imageUrl,
                               CategoryDto category,
                               List<SkuDto> skus,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
}
