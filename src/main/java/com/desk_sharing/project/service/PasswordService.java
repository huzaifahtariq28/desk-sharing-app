package com.desk_sharing.project.service;

import com.desk_sharing.project.bean.entity.User;
import com.desk_sharing.project.bean.request.ResetPasswordRequest;
import com.desk_sharing.project.dao.UserCrudRepo;
import com.desk_sharing.project.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordService {

    @Autowired
    UserService userService;

    @Autowired
    UserCrudRepo userCrudRepo;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    private MailService mailService;

    @Value("${" + Constants.APP_URL + "}")
    String appUrl;

    private static final Logger LOGGER = LogManager.getLogger(PasswordService.class);
    public JSONObject resetPassword(String username, ResetPasswordRequest request) {
        LOGGER.info("user: '{}', resetting password", username);

        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        User user = userService.findByUserName(username);
        long id = user.getId();

        List<String> validationFailures = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        try {
            LOGGER.info("authenticating provided password");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            oldPassword
                    )
            );

            validationFailures = updatePassword(user, newPassword);
        } catch (Exception e) {
            LOGGER.error("Incorrect Old Password provided!");
            validationFailures.add("Incorrect Old Password provided!");
        }


        jsonObject.put("id", id);
        jsonObject.put("validationFailures", validationFailures);

        return jsonObject;
    }

    public long resetPassword(long userId) {
        LOGGER.info("admin, resetting password for user id : {}", userId);

        User user = userCrudRepo.findById(userId);

        String newPassword = generateRandomPassword();

        user.setPassword(passwordEncoder.encode(newPassword));

        String body = passwordResetTemplate(user.getName());
        mailService.sendPasswordEmail(body, user.getEmail(), user.getUsername(), newPassword);

        userCrudRepo.save(user);

        LOGGER.info("password reset successful for user id : {}", userId);
        return userId;
    }

    public List<String> updatePassword(User user, String newPassword) {
        List<String> validationFailures = new ArrayList<>();
        List<Rule> rules = new ArrayList<>();
        //Rule 1: Password length should be in between
        //8 and 16 characters
        rules.add(new LengthRule(8, 16));
        //Rule 2: No whitespace allowed
        rules.add(new WhitespaceRule());
        //Rule 3.a: At least one Upper-case character
        rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        //Rule 3.b: At least one Lower-case character
        rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        //Rule 3.c: At least one digit
        rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));

        PasswordValidator validator = new PasswordValidator(rules);
        PasswordData password = new PasswordData(newPassword);

        LOGGER.info("validating new password");
        RuleResult result = validator.validate(password);

        if (!result.isValid()) {
            LOGGER.error("validation failed");
            validationFailures = validator.getMessages(result);
        } else {
            LOGGER.info("validation successful");
            user.setPassword(passwordEncoder.encode(newPassword));
            userCrudRepo.save(user);
        }
        return validationFailures;
    }

    public static String generateRandomPassword() {
        CharacterRule alphabets = new CharacterRule(EnglishCharacterData.Alphabetical);
        CharacterRule digits = new CharacterRule(EnglishCharacterData.Digit);

        PasswordGenerator passwordGenerator = new PasswordGenerator();
        return (passwordGenerator.generatePassword(8, alphabets, digits));
    }

    private String passwordResetTemplate(String firstName) {

        Context context = new Context();
        context.setVariable("firstname", firstName);
        context.setVariable("appURL", appUrl);
        return templateEngine.process("PasswordReset", context);
    }
}
