package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ユーザー名でユーザーを検索するメソッド
    Optional<User> findByName(String name);

    // メールアドレスでユーザーを検索するメソッド
    Optional<User> findByEmail(String email);

    // メールアドレスでユーザーが存在するかどうかをチェックするメソッド
    boolean existsByEmail(String email);
}
