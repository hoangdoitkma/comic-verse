package com.datn.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.password-reset.sender-name:ComicVerse}")
    private String senderName;

    public void sendPasswordResetOtp(String to, String displayName, String otp, int expiresInMinutes) {
        if (!StringUtils.hasText(mailHost)) {
            log.warn("MAIL_HOST is not configured. Password reset OTP for {} is {} and expires in {} minutes.",
                    to, otp, expiresInMinutes);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (StringUtils.hasText(mailUsername)) {
            message.setFrom(mailUsername);
        }
        message.setTo(to);
        message.setSubject(senderName + " password reset code");
        message.setText(buildResetBody(displayName, otp, expiresInMinutes));
        mailSender.send(message);
    }

    private String buildResetBody(String displayName, String otp, int expiresInMinutes) {
        String name = StringUtils.hasText(displayName) ? displayName : "ComicVerse reader";
        return "Hi " + name + ",\n\n"
                + "Your ComicVerse password reset code is: " + otp + "\n"
                + "This code expires in " + expiresInMinutes + " minutes.\n\n"
                + "If you did not request this, you can safely ignore this email.\n\n"
                + senderName;
    }
}
