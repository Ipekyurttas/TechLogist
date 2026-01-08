package org.tech.techlogist.dto.auth;

import lombok.Data;

@Data
public class LoginResponseDto {
    private String token;
    private Long userId;
}
