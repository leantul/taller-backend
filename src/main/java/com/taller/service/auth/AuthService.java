package com.taller.service.auth;

import com.taller.model.repository.AppUserRepository;
import com.taller.resource.dto.auth.ChangePasswordRequestDTO;
import com.taller.resource.dto.auth.LoginRequestDTO;
import com.taller.resource.dto.auth.LoginResponseDTO;
import com.taller.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        var user = appUserRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        return LoginResponseDTO.builder()
                .token(jwtService.generateToken(userDetails))
                .username(user.getUsername())
                .fullName(user.getFullName())
                .build();
    }

    public void changePassword(ChangePasswordRequestDTO request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = appUserRepository.findByUsername(username).orElseThrow();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(user);
    }
}
