package com.taller.model.repository.projection;

import java.time.LocalDate;

public interface ClientDetailView {
    String getId();
    String getName();
    String getLastName();
    String getDni();
    String getEmail();
    String getAddress();
    String getPhone();
    String getNotes();
    LocalDate getBirthDate();
}
