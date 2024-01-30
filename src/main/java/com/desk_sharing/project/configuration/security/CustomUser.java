package com.desk_sharing.project.configuration.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
public class CustomUser extends User {

    private long userId;
    private String email;
    private String company;


    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities, long userId, String email) {
        super(username, password, authorities);
        this.userId = userId;
        this.email = email;
    }
}
