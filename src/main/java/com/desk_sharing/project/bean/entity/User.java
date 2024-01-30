package com.desk_sharing.project.bean.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements RowMapper<User> {
    private long id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private String name;
    private Boolean isAdmin;
    private Boolean isActive;

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("name"),
                rs.getBoolean("isAdmin"),
                rs.getBoolean("isActive")
        );
    }
}
