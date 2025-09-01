package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.VerificationToken;

import java.util.Optional;

public interface VerificationTokenDao {

    void save(VerificationToken token);

    Optional<VerificationToken> findByToken(String token);

    void delete(VerificationToken token);
}
