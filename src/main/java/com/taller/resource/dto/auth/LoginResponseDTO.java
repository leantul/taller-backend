package com.taller.resource.dto.auth;


public class LoginResponseDTO {
    private String token;
    private String username;
    private String fullName;

    public LoginResponseDTO(String token, String username, String fullName) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
    }

    public String getToken() {
        return this.token;
    }

    public String getUsername() {
        return this.username;
    }

    public String getFullName() {
        return this.fullName;
    }

    public static LoginResponseDTOBuilder builder() {
        return new LoginResponseDTOBuilder();
    }

    public static class LoginResponseDTOBuilder {
        private String token;
        private String username;
        private String fullName;

        public LoginResponseDTOBuilder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponseDTOBuilder username(String username) {
            this.username = username;
            return this;
        }

        public LoginResponseDTOBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public LoginResponseDTO build() {
            return new LoginResponseDTO(token, username, fullName);
        }
    }
}
