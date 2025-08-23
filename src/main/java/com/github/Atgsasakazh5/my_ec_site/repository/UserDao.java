package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.User;

import java.util.Optional;

interface UserDao {
    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}
