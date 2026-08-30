package com.taller.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "clients")
@Entity
@AttributeOverride(name = "id", column = @Column(name = "id_client"))
public class Client extends BasicEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "reference", columnDefinition = "TEXT")
    private String reference;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "notes")
    private String notes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_phones", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "phone")
    private List<String> phones = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_emails", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "email")
    private List<String> emails = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    private List<Repair> repairs;

    @OneToMany(mappedBy = "client")
    private List<Device> devices;

    @OneToMany(mappedBy = "client")
    private List<FollowUp> followUps;
}
