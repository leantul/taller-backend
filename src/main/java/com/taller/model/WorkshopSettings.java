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
@Table(name = "workshop_settings")
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_workshop_settings"))
public class WorkshopSettings extends BasicEntity {

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "whatsapp")
    private String whatsapp;

    @Column(name = "instagram")
    private String instagram;

    @Column(name = "logo_asset_path", nullable = false)
    private String logoAssetPath;
}
