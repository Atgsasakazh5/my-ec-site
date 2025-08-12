package com.github.Atgsasakazh5.my_ec_site.entity;

public enum OrderStatus {
    PENDING("保留中"),
    PAID("支払い済み"),
    SHIPPED("発送済み"),
    DELIVERED("配達済み"),
    CANCELED("キャンセル済み");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
