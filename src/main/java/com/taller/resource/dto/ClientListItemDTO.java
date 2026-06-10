package com.taller.resource.dto;

public record ClientListItemDTO(
        String id,
        String name,
        String lastName,
        String email,
        String phone,
        long deviceCount
) {
}
