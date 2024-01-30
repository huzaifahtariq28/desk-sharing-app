package com.desk_sharing.project.service;

import com.desk_sharing.project.utils.Constants;
import com.desk_sharing.project.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class MailService {

    @Value("${" + Constants.EMAIL_FROM + "}")
    String emailFrom;

    @Value("${" + Constants.APP_URL + "}")
    String appUrl;

    @Autowired
    JavaMailSender emailSender;

    private static final String FILE_NAME = "credentials";

    private static final String EMAIL_SUBJECT = "Credentials for Desk Sharing";

    private static final String[] HEADERS = {"username", "password", "Application URL"};

    private static final String DOT_CSV = ".csv";

    private static final Logger LOGGER = LogManager.getLogger(MailService.class);

    public void sendPasswordEmail(String body, String email, String userName, String password) {
        byte[] data = Utils.getPasswordInCSV(userName, password, appUrl, HEADERS);

        Runnable runnable = () -> {
            try {
                sendMessageWithAttachment(email, EMAIL_SUBJECT, body, FILE_NAME + DOT_CSV, data);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void sendMessageWithAttachment(String to, String subject, String text, String attachmentName, byte[] attachment) throws MessagingException {

        LOGGER.info("sending email to: {}", to);

        MimeMessage message = emailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(emailFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        helper.addAttachment(attachmentName, new ByteArrayResource(attachment));

        emailSender.send(message);
        LOGGER.info("email sent to : {}", to);
    }

}
