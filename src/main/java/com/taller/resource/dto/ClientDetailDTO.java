package com.taller.resource.dto;

import java.time.LocalDate;
import java.util.List;

public record ClientDetailDTO(
        String id,
        String name,
        String lastName,
        String reference,
        String email,
        String address,
        String phone,
        String notes,
        List<String> phones,
        List<String> emails,
        LocalDate birthDate
) {
}
