package org.tech.techlogist.dto.user;

import lombok.Data;
import org.tech.techlogist.enums.Role;

@Data
public class UserUpdateRequestDto {

    private String username;
    private String email;
    private String password;
    private Role role;
}
