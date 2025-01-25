package com.taller.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Table(name = "clients")
@Entity
@AttributeOverride(name = "id", column = @Column(name = "id_client"))
public class Client extends BasicEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @OneToMany(mappedBy = "client")
    private List<Repair> repairs;

    @OneToMany(mappedBy = "client")
    private List<Device> devices;
}
