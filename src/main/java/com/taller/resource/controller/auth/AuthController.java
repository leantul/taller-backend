package com.taller.resource.controller.auth;

import com.taller.resource.dto.auth.ChangePasswordRequestDTO;
import com.taller.resource.dto.auth.LoginRequestDTO;
import com.taller.resource.dto.auth.LoginResponseDTO;
import com.taller.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(originPatterns = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
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
}
