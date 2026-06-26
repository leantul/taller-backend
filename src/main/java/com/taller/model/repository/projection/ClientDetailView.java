package com.taller.model.repository.projection;

import java.time.LocalDate;

public interface ClientDetailView {
    String getId();
    String getName();
    String getLastName();
    String getReference();
    String getEmail();
    String getAddress();
    String getPhone();
    String getNotes();
    LocalDate getBirthDate();
}
