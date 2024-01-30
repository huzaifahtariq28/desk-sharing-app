package com.desk_sharing.project.bean.model;

import com.desk_sharing.project.utils.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@NoArgsConstructor
@Getter
public class AuthToken {
    @Id
    private String token;

    public AuthToken(String token) {
        this.token = token;
    }
}
