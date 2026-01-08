package org.tech.techlogist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tech.techlogist.dto.user.UserRegisterRequestDto;
import org.tech.techlogist.dto.user.UserResponseDto;
import org.tech.techlogist.dto.user.UserUpdateRequestDto;
import org.tech.techlogist.entity.User;
import org.tech.techlogist.enums.Role;
import org.tech.techlogist.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserResponseDto register(UserRegisterRequestDto dto) {

        checkUnique(dto.getUsername(), dto.getEmail());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.CUSTOMER);

        return map(userRepository.save(user));
    }


    public UserResponseDto createByAdmin(UserUpdateRequestDto dto) {

        if (dto.getRole() == null)
            throw new RuntimeException("Role must be provided");

        checkUnique(dto.getUsername(), dto.getEmail());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());

        return map(userRepository.save(user));
    }


    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    private void checkUnique(String username, String email) {
        if (userRepository.findByUsername(username).isPresent())
            throw new RuntimeException("Username already exists");
        if (userRepository.findByEmail(email).isPresent())
            throw new RuntimeException("Email already exists");
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
    }

    private UserResponseDto map(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }
}
