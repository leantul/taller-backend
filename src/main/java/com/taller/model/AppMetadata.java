package com.taller.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_metadata")
public class AppMetadata {

    @Id
    @Column(name = "metadata_key", nullable = false, length = 120)
    private String key;

    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private String value;

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
