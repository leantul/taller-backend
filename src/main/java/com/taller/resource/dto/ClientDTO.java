package com.taller.resource.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class ClientDTO {
    private String id;
    private String name;
    private String lastName;
    private String dni;
    private String email;
    private String address;
    private String phone;
    private String notes;
    private List<String> phones;
    private List<String> emails;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    public String getId() {
        return this.id;
    }

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

    public String getNotes() {
        return this.notes;
    }

    public List<String> getPhones() {
        return this.phones;
    }

    public List<String> getEmails() {
        return this.emails;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
