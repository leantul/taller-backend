package com.taller.resource.dto;

import com.taller.model.enums.FollowUpStatusEnum;
import java.time.LocalDate;

public record FollowUpListItemDTO(
        String id,
        String clientId,
        String displayName,
        String contactChannel,
        String contactValue,
        String deviceDescription,
        LocalDate nextContactDate,
        LocalDate currentPromisedDate,
        FollowUpStatusEnum status,
        long commitmentCount,
        long missedCommitmentCount
) { }
