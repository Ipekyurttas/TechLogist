package org.tech.techlogist.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequestDto {

    private String username;
    private String email;
    private String password;
}
