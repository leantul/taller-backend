package com.taller.resource.controller.auth;

import com.taller.resource.dto.auth.ChangePasswordRequestDTO;
import com.taller.resource.dto.auth.LoginRequestDTO;
import com.taller.resource.dto.auth.LoginResponseDTO;
import com.taller.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequestDTO request) {
        authService.changePassword(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
}
