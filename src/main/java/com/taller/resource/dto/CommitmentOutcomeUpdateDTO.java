package com.taller.resource.dto;

import com.taller.model.enums.CommitmentOutcomeEnum;
import jakarta.validation.constraints.NotNull;

public record CommitmentOutcomeUpdateDTO(
        @NotNull(message = "El resultado es obligatorio") CommitmentOutcomeEnum outcome
) { }
