package com.desk_sharing.project.service;

import com.desk_sharing.project.bean.dto.UserDto;
import com.desk_sharing.project.bean.entity.User;
import com.desk_sharing.project.bean.request.ResetPasswordRequest;
import com.desk_sharing.project.configuration.security.CustomUser;
import com.desk_sharing.project.dao.UserCrudRepo;
import com.desk_sharing.project.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.passay.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {
    private static final Logger LOGGER = LogManager.getLogger(UserService.class);
    
    @Autowired
    UserCrudRepo userCrudRepo;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    public List<User> findAll() {

        return userCrudRepo.getAllUsers();
    }

    public User save(UserDto userDto) {
        LOGGER.info("creating new user, username: {}", userDto.getUsername());
        User user = userDto.getUserFromDto();
        User testUser = userCrudRepo.findByUsername(user.getUsername());
        if (null != testUser) {
            LOGGER.error("username is already taken.");
            throw new IllegalArgumentException("Username is already taken.");
        }
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        LOGGER.info("new user created successfully");
        return userCrudRepo.save(user);
    }

    public User updateUser(UserDto userDto) {
        User user = userCrudRepo.findById(userDto.getId());
        User nUser = userDto.getUserFromDto();

        Set<String> ignoreProperties = Utils.getNullPropertyNames(nUser);

        LOGGER.info("setting ignoreProperties, which will never be updated");
        ignoreProperties.add("id");
        ignoreProperties.add("username");

        LOGGER.info("skipping ignoreProperties, which will never be updated");
        String[] skipFields = Utils.stringSetToStringArray(ignoreProperties);

        LOGGER.info("updating the user with new fields");
        BeanUtils.copyProperties(nUser, user, skipFields);

        return userCrudRepo.save(user);
    }

    public long updateUserRole(long id, boolean isAdmin) {
        LOGGER.info("updating 'isAdmin' status for user id: {}", id);
        User user = userCrudRepo.findById(id);

        if (user != null) {
            LOGGER.info("updating user '{}' isAdmin status to: {}", user.getUsername(), isAdmin);
            user.setIsAdmin(isAdmin);
            userCrudRepo.save(user);
        } else {
            id = -1;
            LOGGER.error("No user found for user id: {}", id);
            throw new IllegalArgumentException("User not found");
        }
        return id;
    }

    public long updateUserStatus(long id, boolean isActive) {
        LOGGER.info("updating 'isActive' status for user id: {}", id);
        User user = userCrudRepo.findById(id);

        if (user != null) {
            LOGGER.info("updating user '{}' isActive status to: {}", user.getUsername(), isActive);
            user.setIsActive(isActive);
            userCrudRepo.save(user);
        } else {
            id = -1;
            LOGGER.error("No user found for user id: {}", id);
            throw new IllegalArgumentException("User not found");
        }
        return id;
    }

    public long deleteUser(long id) {
        LOGGER.info("deleting user of user id: {}", id);
        User user = userCrudRepo.findById(id);

        if (user != null) {
            userCrudRepo.delete(id);
        } else {
            id = -1;
            LOGGER.error("No user found for user id: {}", id);
            throw new IllegalArgumentException("User not found");
        }
        return id;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUserName(username);
        return new CustomUser(user.getUsername(), user.getPassword() == null ? "" : user.getPassword(), Collections.singleton(getAuthority(user)), user.getId(), user.getEmail());
    }

    public User findByUserName(String username) {
        LOGGER.info("finding by username: {}", username);
        User user = userCrudRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return user;
    }

    private SimpleGrantedAuthority getAuthority(User user) {

        LOGGER.info("getting authorities of user: {}", user.getUsername());
        return new SimpleGrantedAuthority(Boolean.TRUE.equals(user.getIsAdmin()) ? "Admin" : "Normal");
    }
}
