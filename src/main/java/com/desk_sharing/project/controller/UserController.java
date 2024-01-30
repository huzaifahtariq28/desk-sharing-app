package com.desk_sharing.project.controller;

import com.desk_sharing.project.bean.dto.UserDto;
import com.desk_sharing.project.bean.entity.User;
import com.desk_sharing.project.bean.model.AuthToken;
import com.desk_sharing.project.bean.request.LoginUserRequest;
import com.desk_sharing.project.bean.request.ResetPasswordRequest;
import com.desk_sharing.project.configuration.security.CustomUser;
import com.desk_sharing.project.service.AuthenticationService;
import com.desk_sharing.project.service.PasswordService;
import com.desk_sharing.project.service.UserService;
import com.desk_sharing.project.utils.Constants;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    PasswordService passwordService;

    @PostMapping("/authenticate")
    public ResponseEntity<Object> authenticate(HttpServletResponse response, @RequestBody LoginUserRequest loginUser) {

        Cookie cookie = authenticationService.authenticate(loginUser);

        response.addCookie(cookie);
        response.addHeader("Access-Control-Allow-Credentials", "true");
        return ResponseEntity.ok(new AuthToken(cookie.getValue()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(Authentication auth, HttpServletResponse response) {

        CustomUser user = (CustomUser) auth.getPrincipal();
        Cookie cookie = authenticationService.logout(user.getUserId());
        response.addCookie(cookie);

        return ResponseEntity.ok("Logged Out!");
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping("/update")
    public User updateUser(@RequestBody UserDto user) {
        return userService.updateUser(user);
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/resetPassword")
    public JSONObject resetPassword(Authentication authentication, @RequestBody ResetPasswordRequest body) {
        return passwordService.resetPassword(authentication.getName(), body);
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/reset/{userId}")
    public long resetPasswordByAdmin(@PathVariable long userId) throws Exception {
        return passwordService.resetPassword(userId);
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
        HttpStatus status = HttpStatus.OK;
        User user;
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            List<String> message = new ArrayList<>();
            for (FieldError e : errors) {
                message.add("Invalid " + e.getField() + ". " + e.getDefaultMessage());
            }
            status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(message, status);
        }
        try {
            user = userService.save(userDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return new ResponseEntity<>(user, status);
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/roles")
    public long updateUserRoles(@RequestBody UserDto user) {
        return userService.updateUserRole(user.getId(), user.isAdmin());
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/status")
    public long updateUserStatus(@RequestBody UserDto user) {
        return userService.updateUserStatus(user.getId(), user.isActive());
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{userId}")
    public long deleteUser(@RequestParam long userId) {
        return userService.deleteUser(userId);
    }
}
