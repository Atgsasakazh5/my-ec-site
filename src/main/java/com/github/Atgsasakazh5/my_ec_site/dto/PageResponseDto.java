package com.github.Atgsasakazh5.my_ec_site.dto;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        int totalElements,
        int totalPages
) {
    // 総ページ数を計算する
    public PageResponseDto(List<T> content, int page, int size, int totalElements) {
        this(content, page, size, totalElements, (int) Math.ceil((double) totalElements / size));
    }
}
