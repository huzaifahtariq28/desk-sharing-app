package com.desk_sharing.project.configuration.security;


import com.desk_sharing.project.bean.entity.User;
import com.desk_sharing.project.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Stream;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.header.string}")
    public String headerString;

    @Value("${jwt.token.prefix}")
    public String tokenPrefix;

    public static final String TOKEN = "TOKEN";

    @Autowired
    UserService userService;

    @Autowired
    private TokenProvider jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(headerString);
        String username = null;
        String authToken = null;

        // if token given in AUTHORIZATION header
        if (header != null && header.startsWith(tokenPrefix)) {
            authToken = header.substring(tokenPrefix.length() + 1); // + 1 for space between bearer and token
        } else { // if token given in Cookie

            logger.warn("Couldn't find bearer string, header will be ignored. Checking for cookie based token now...");
            Cookie[] cookies = req.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (TOKEN.equals(cookie.getName())) {
                        authToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        try {
            username = jwtTokenUtil.getUsernameFromToken(authToken);
            User user = userService.findByUserName(username);
            if (Boolean.FALSE.equals(user.getIsActive())) {
                throw new DisabledException("The user is in-active. Please contact admin");
            }
        } catch (IllegalArgumentException e) {
            logger.error("An error occurred while fetching Username from Token", e);
        } catch (ExpiredJwtException e) {
            logger.warn("The token has expired", e);
        } catch (SignatureException e) {
            logger.error("Authentication Failed. Username or Password not valid.", e);
        } catch (DisabledException e) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.getWriter().write("Account is disabled!");
            return;
        } catch (CredentialsExpiredException e) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.getWriter().write("Credentials are expired!");
            return;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userService.loadUserByUsername(username);

            if (Boolean.TRUE.equals(jwtTokenUtil.validateToken(authToken, userDetails))) {
                SecurityContextHolder.getContext().getAuthentication();
                UsernamePasswordAuthenticationToken authentication = jwtTokenUtil.getAuthenticationToken(authToken, userDetails);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                logger.info("authenticated user " + username + ", setting security context");
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String url = request.getRequestURI();
        String[] excludedUrls = {
                "/users/authenticate",
                "/users/signup"
        };
        return Stream.of(excludedUrls).anyMatch(x -> new AntPathMatcher().match(x, url));
    }
}