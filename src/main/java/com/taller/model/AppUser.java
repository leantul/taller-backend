package com.taller.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
@AttributeOverride(name = "id", column = @Column(name = "id_user"))
public class AppUser extends BasicEntity {

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    public AppUser() {
    }

    public AppUser(String username, String passwordHash, String fullName, Boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.enabled = enabled;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public String getFullName() {
        return this.fullName;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public static AppUserBuilder builder() {
        return new AppUserBuilder();
    }

    public static class AppUserBuilder {
        private String username;
        private String passwordHash;
        private String fullName;
        private Boolean enabled;

        public AppUserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AppUserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public AppUserBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public AppUserBuilder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public AppUser build() {
            return new AppUser(username, passwordHash, fullName, enabled);
        }
    }
}
