package com.taller.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_metadata")
public class AppMetadata {

    @Id
    @Column(name = "metadata_key", nullable = false, length = 120)
    private String key;

    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private String value;
}
