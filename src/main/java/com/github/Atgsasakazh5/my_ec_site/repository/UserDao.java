package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Role;
import com.github.Atgsasakazh5.my_ec_site.entity.User;

import java.util.Optional;
import java.util.Set;

public interface UserDao {
    Set<Role> findRolesByUserId(Long userId);

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}
