package org.tech.techlogist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tech.techlogist.dto.user.UserResponseDto;
import org.tech.techlogist.dto.user.UserUpdateRequestDto;
import org.tech.techlogist.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    public UserResponseDto createUserByAdmin(
            @RequestBody UserUpdateRequestDto dto) {

        return userService.createByAdmin(dto);
    }

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
