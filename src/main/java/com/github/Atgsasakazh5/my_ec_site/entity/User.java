package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class User {

    private Long id;

    private String name;

    private String email;

    @ToString.Exclude   // ログにパスワードが出力されないようにする
    private String password;

    private String address;

    private boolean emailVerified;

    private boolean subscribingNewsletter;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<Role> roles = new HashSet<>();  // ユーザーのロールを保持するセット
}
