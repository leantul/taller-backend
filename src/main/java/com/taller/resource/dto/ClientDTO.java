package com.taller.resource.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
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
}
