package com.taller.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "clients")
@Entity
@AttributeOverride(name = "id", column = @Column(name = "id_client"))
public class Client extends BasicEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "dni")
    private String dni;

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

    public String getName() {
        return this.name;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getDni() {
        return this.dni;
    }

    public String getEmail() {
        return this.email;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPhone() {
        return this.phone;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public String getNotes() {
        return this.notes;
    }

    public List<String> getPhones() {
        return this.phones;
    }

    public List<String> getEmails() {
        return this.emails;
    }

    public List<Repair> getRepairs() {
        return this.repairs;
    }

    public List<Device> getDevices() {
        return this.devices;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public void setRepairs(List<Repair> repairs) {
        this.repairs = repairs;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }
}
