package com.taller.model.repository.projection;

public interface ClientListView {
    String getId();
    String getName();
    String getLastName();
    String getPhone();
    long getDeviceCount();
    long getRepairCount();
}
