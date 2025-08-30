package com.github.Atgsasakazh5.my_ec_site.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}
