package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderDao {

    Order saveOrder(Order order);

    Optional<Order> findOrderById(Long orderId);

    List<Order> findAllOrdersByUserId(Long userId);
}
