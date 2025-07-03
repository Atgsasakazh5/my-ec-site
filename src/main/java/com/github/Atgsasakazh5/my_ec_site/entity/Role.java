package com.github.Atgsasakazh5.my_ec_site.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING) // Enumを文字列としてDBに保存する設定
    @Column(nullable = false, unique = true, length = 50)
    private RoleName name;
}