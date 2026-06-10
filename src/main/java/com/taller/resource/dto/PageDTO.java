package com.taller.resource.dto;

import java.util.List;

public record PageDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public PageDTO {
        content = List.copyOf(content);
    }
}
