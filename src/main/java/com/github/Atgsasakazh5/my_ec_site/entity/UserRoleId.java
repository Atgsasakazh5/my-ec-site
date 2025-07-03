package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * UserRoleエンティティの複合主キーを表すクラス
 */
@Data // @Getter, @Setter, @EqualsAndHashCode, @ToStringなどをまとめて生成
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleId implements Serializable {

    // Userエンティティの主キーの型に合わせる
    private Long user;

    // Roleエンティティの主キーの型に合わせる
    private Integer role;
}