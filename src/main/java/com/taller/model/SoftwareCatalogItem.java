package com.taller.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@Table(name = "software_catalog_items")
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_software_catalog_item"))
public class SoftwareCatalogItem extends BasicEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "detail")
    private String detail;
}
