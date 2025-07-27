package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Role {

    private Integer id;

    private RoleName name;
}