package com.taller.resource.dto;

import com.taller.model.enums.FollowUpStatusEnum;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record FollowUpSaveDTO(
        String id,
        String clientId,
        String contactName,
        String contactChannel,
        String contactValue,
        @NotBlank(message = "La descripción del equipo es obligatoria") String deviceDescription,
        String reportedProblem,
        LocalDate nextContactDate,
        FollowUpStatusEnum status,
        String notes,
        LocalDate initialPromisedDate,
        String initialPromiseNotes
) { }
