package com.taller.resource.dto;

public record ClientHistoryDTO(
        ClientDetailDTO client,
        PageDTO<ClientRepairHistoryItemDTO> repairs
) {
}
