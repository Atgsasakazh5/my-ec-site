package com.github.Atgsasakazh5.my_ec_site.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@IdClass(UserRoleId.class) // UserRoleIdの複合主キークラスを指定
public class UserRole {

    @Id // このフィールドが複合主キーの一部であることを示す
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // DB上の外部キーカラム名を指定
    private User user;

    @Id // このフィールドも複合主キーの一部であることを示す
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id") // DB上の外部キーカラム名を指定
    private Role role;
}