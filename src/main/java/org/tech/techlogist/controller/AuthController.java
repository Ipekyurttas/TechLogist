package org.tech.techlogist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.tech.techlogist.config.JwtUtil;
import org.tech.techlogist.dto.auth.LoginRequestDto;
import org.tech.techlogist.dto.auth.LoginResponseDto;
import org.tech.techlogist.dto.user.UserRegisterRequestDto;
import org.tech.techlogist.dto.user.UserResponseDto;
import org.tech.techlogist.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/register")
    public UserResponseDto register(
            @RequestBody UserRegisterRequestDto dto) {

        return userService.register(dto);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        var user = userService.findByUsername(dto.getUsername());
        Long userId = user.getId();


        String role = "CUSTOMER";
        if ("ipek".equals(dto.getUsername())) {
            role = "ADMIN";
        }

        String token = jwtUtil.generateToken(dto.getUsername(), role);

        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setUserId(userId);

        return response;
    }
}
