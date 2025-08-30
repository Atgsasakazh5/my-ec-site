package com.github.Atgsasakazh5.my_ec_site.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("メールアドレスの本人確認");
        message.setText("こちらのリンクからメールアドレスの本人確認を行ってください: "
                + "http://localhost:8080/api/auth/verify?token=" + token);
        mailSender.send(message);
    }
}
