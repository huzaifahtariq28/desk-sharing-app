package com.desk_sharing.project.service;

import com.desk_sharing.project.bean.entity.User;
import com.desk_sharing.project.bean.request.LoginUserRequest;
import com.desk_sharing.project.configuration.security.CustomUser;
import com.desk_sharing.project.configuration.security.TokenProvider;
import com.desk_sharing.project.dao.UserCrudRepo;
import com.desk_sharing.project.utils.Constants;
import com.desk_sharing.project.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.util.Date;

@Service
public class AuthenticationService {
    private static final Logger LOGGER = LogManager.getLogger(AuthenticationService.class);
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    TokenProvider jwtTokenUtil;
    @Autowired
    UserService userService;
    @Autowired
    private Environment env;
    @Value("${" + Constants.CONFIG_ENABLE_HTTPS_SECURE_COOKIES + "}")
    boolean isSecureEnabled;
    public Cookie authenticate(LoginUserRequest loginUser) {
        User user = userService.findByUserName(loginUser.getUsername());
        if (null != user && !user.getIsActive()) {
            throw new DisabledException("The user is in-active. Please contact admin");
        }

        LOGGER.info("authenticating user by credentials for username: {}", loginUser.getUsername());
        final Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginUser.getUsername(),
                            loginUser.getPassword()
                    )
            );
        }
        catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Credentials.");
        }
        LOGGER.info("setting user in security context holder");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LOGGER.info("generating JWT token");
        final String token = jwtTokenUtil.generateToken(authentication);

        return Utils.setCookie(token, false, isSecureEnabled, env.getProperty(Constants.JWT_TOKEN_VALIDITY));
    }

    public Cookie logout(long userId) {
        LOGGER.info("logging out userid : {}", userId);
        return Utils.setCookie(null, true, isSecureEnabled, "");
    }
}
