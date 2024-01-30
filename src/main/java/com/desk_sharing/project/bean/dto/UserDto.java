package com.desk_sharing.project.bean.dto;

import com.desk_sharing.project.bean.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private long id;
    @NotBlank(message = "User name should not be blank")
    @NotEmpty(message = "User name should not be empty")
    private String username;
    private String password;
    @Email(message = "Must be a valid email address")
    private String email;
    private String name;
    private boolean active;
    private boolean admin;

    public User getUserFromDto() {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setName(name);
        user.setIsActive(active);
        user.setIsAdmin(admin);
        return user;
    }
}
