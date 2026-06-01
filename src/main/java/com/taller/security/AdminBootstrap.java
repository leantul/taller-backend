package com.taller.security;

import com.taller.model.AppUser;
import com.taller.model.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.username:admin}")
    private String defaultUsername;

    @Value("${app.bootstrap.admin.password:}")
    private String defaultPassword;

    @Value("${app.bootstrap.admin.fullname:Administrador Taller}")
    private String defaultFullName;

    @Override
    public void run(String... args) {
        if (defaultPassword == null || defaultPassword.isBlank()) {
            return;
        }

        appUserRepository.findByUsername(defaultUsername).orElseGet(() -> {
            AppUser appUser = AppUser.builder()
                    .username(defaultUsername)
                    .passwordHash(passwordEncoder.encode(defaultPassword))
                    .fullName(defaultFullName)
                    .enabled(true)
                    .build();
            return appUserRepository.save(appUser);
        });
    }

    public AdminBootstrap(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }
}
