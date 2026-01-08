package org.tech.techlogist.dto.user;

import lombok.Data;

@Data
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private String role;
}
