package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.dto.OrderDetailDto;
import com.github.Atgsasakazh5.my_ec_site.entity.OrderDetail;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderDetailDaoImpl implements OrderDetailDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public OrderDetailDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<OrderDetail> orderDetailRowMapper = (rs, rowNum) ->
            new OrderDetail(rs.getLong("id"), rs.getLong("order_id"),
                    rs.getLong("sku_id"), rs.getInt("quantity"),
                    rs.getInt("price_at_order"));

    @Override
    public void save(List<OrderDetail> orderDetails) { // 戻り値をvoidに変更
        String sql = "INSERT INTO order_details (order_id, sku_id, quantity, price_at_order) " +
                "VALUES (:orderId, :skuId, :quantity, :priceAtOrder)";

        List<Map<String, Number>> batchValues = orderDetails.stream()
                .map(detail -> {
                    Map<String, Number> map = new HashMap<>();
                    map.put("orderId", detail.getOrderId());
                    map.put("skuId", detail.getSkuId());
                    map.put("quantity", detail.getQuantity());
                    map.put("priceAtOrder", detail.getPriceAtOrder());
                    return map;
                })
                .toList();

        namedParameterJdbcTemplate.batchUpdate(sql, batchValues.toArray(new Map[0]));
    }

    @Override
    public List<OrderDetailDto> findByOrderId(Long orderId) {
        String sql = "SELECT od.id, od.order_id, od.sku_id, p.name AS product_name, " +
                "s.size, s.color, p.image_url, od.price_at_order, od.quantity " +
                "FROM order_details od " +
                "JOIN skus s ON od.sku_id = s.id " +
                "JOIN products p ON s.product_id = p.id " +
                "WHERE od.order_id = :orderId";

        Map<String, Long> params = new HashMap<>();
        params.put("orderId", orderId);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) ->
                new OrderDetailDto(
                        rs.getLong("id"),
                        rs.getLong("order_id"),
                        rs.getLong("sku_id"),
                        rs.getString("product_name"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getString("image_url"),
                        rs.getInt("price_at_order"),
                        rs.getInt("quantity")
                ));

    }
}
