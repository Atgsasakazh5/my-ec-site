package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {

    private Long id;
    private String token;
    private Long userId;
    private LocalDateTime expiryDate;

}
