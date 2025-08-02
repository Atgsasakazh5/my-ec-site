package com.github.Atgsasakazh5.my_ec_site.dto;

import java.time.LocalDateTime;

public record ProductSummaryDto(Long id,
                                String name,
                                Integer price,
                                String description,
                                String imageUrl,
                                CategoryDto category,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
}
