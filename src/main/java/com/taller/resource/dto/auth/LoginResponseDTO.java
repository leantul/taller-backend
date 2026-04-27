package com.taller.resource.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDTO {
    private String token;
    private String username;
    private String fullName;
}
