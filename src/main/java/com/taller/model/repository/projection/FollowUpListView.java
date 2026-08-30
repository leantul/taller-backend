package com.taller.model.repository.projection;

import com.taller.model.enums.FollowUpStatusEnum;
import java.time.LocalDate;

public interface FollowUpListView {
    String getId();
    String getClientId();
    String getClientName();
    String getClientLastName();
    String getContactName();
    String getContactChannel();
    String getContactValue();
    String getDeviceDescription();
    LocalDate getNextContactDate();
    LocalDate getCurrentPromisedDate();
    FollowUpStatusEnum getStatus();
    Long getCommitmentCount();
    Long getMissedCommitmentCount();
}
