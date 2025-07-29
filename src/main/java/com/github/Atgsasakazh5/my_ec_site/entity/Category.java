package com.github.Atgsasakazh5.my_ec_site.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Category {

    private Integer id;

    private String name;

}
