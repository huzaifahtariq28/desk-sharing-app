package com.desk_sharing.project.dao;

import com.desk_sharing.project.bean.entity.User;
import com.desk_sharing.project.dao.query.UserQueries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class UserCrudRepo {
    private static final Logger LOGGER = LogManager.getLogger(UserCrudRepo.class);
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<User> getAllUsers() {
        String sql = UserQueries.GET_ALL_USERS;
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(), new User());
    }

    public User findById(long id) {
        String sql = UserQueries.GET_BY_ID;
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("id", id);

        return namedParameterJdbcTemplate.queryForObject(sql, sqlParameterSource, new User());
    }

    public User save(User user) {
        try {
            MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
            sqlParameterSource.addValue("username", user.getUsername());
            sqlParameterSource.addValue("password", user.getPassword());
            sqlParameterSource.addValue("email", user.getEmail());
            sqlParameterSource.addValue("name", user.getName());
            sqlParameterSource.addValue("isActive", user.getIsActive());
            sqlParameterSource.addValue("isAdmin", user.getIsAdmin());
            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(UserQueries.UPSERT_USER, sqlParameterSource, keyHolder);
            long userId = Objects.requireNonNull(keyHolder.getKey(), "failed to save userid: [" + user.getId() + "], username: [" + user.getUsername() + "]").longValue();
            user.setId(userId);

        } catch (Exception e) {
            LOGGER.error("Unable to insert user", e);
            user = new User();
            user.setId(-1);
        }

        return user;
    }

    public User findByUsername(String username) {
        User user = null;
        String sql = UserQueries.GET_BY_USERNAME;
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("username", username);

        try {
            user = namedParameterJdbcTemplate.queryForObject(sql, sqlParameterSource, new User());
        } catch (EmptyResultDataAccessException e) {
            LOGGER.error("No user Found");
        } catch (Exception e) {
            LOGGER.error("Exception occurred", e);
        }
        return user;
    }

    public void delete(long id) {
        try {
            MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
            sqlParameterSource.addValue("id", id);
            namedParameterJdbcTemplate.update(UserQueries.DELETE, sqlParameterSource);
        } catch (Exception e) {
            LOGGER.error("Unable to delete user with ID: {}", id);
        }
    }
}
