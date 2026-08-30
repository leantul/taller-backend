package com.taller.resource.dto;

import com.taller.model.enums.FollowUpStatusEnum;
import java.time.LocalDate;
import java.util.List;

public record FollowUpDetailDTO(
        String id,
        String clientId,
        String clientName,
        String contactName,
        String contactChannel,
        String contactValue,
        String deviceDescription,
        String reportedProblem,
        LocalDate nextContactDate,
        FollowUpStatusEnum status,
        String notes,
        List<FollowUpCommitmentDTO> commitments
) { }
