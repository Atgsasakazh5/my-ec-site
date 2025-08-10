package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.dto.OrderDetailDto;
import com.github.Atgsasakazh5.my_ec_site.entity.OrderDetail;

import java.util.List;

public interface OrderDetailDao {

    void save(List<OrderDetail> orderDetails);

    List<OrderDetailDto> findByOrderId(Long orderId);
}
