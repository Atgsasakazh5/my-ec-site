package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.dto.OrderSummaryDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Order;
import com.github.Atgsasakazh5.my_ec_site.entity.OrderStatus;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OrderDaoImpl implements OrderDao {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public OrderDaoImpl(JdbcTemplate jdbcTemplate,
                        NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) ->
            new Order(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    OrderStatus.valueOf(rs.getString("status")),
                    rs.getInt("total_price"),
                    rs.getString("shipping_address"),
                    rs.getString("shipping_postal_code"),
                    rs.getString("shipping_name"),
                    rs.getTimestamp("ordered_at").toLocalDateTime()
            );

    @Override
    public Order saveOrder(Order order) {
        LocalDateTime now = LocalDateTime.now();
        order.setOrderedAt(now);
        String sql = "INSERT INTO orders (user_id, status, total_price, shipping_address, shipping_postal_code, shipping_name, ordered_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, order.getUserId());
            ps.setString(2, order.getStatus().name());
            ps.setInt(3, order.getTotalPrice());
            ps.setString(4, order.getShippingAddress());
            ps.setString(5, order.getPostalCode());
            ps.setString(6, order.getShippingName());
            ps.setTimestamp(7, Timestamp.valueOf(order.getOrderedAt()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            order.setId(key.longValue());
        } else {
            throw new IllegalStateException("データベースから生成されたIDの取得に失敗しました。");
        }
        Long generatedId = keyHolder.getKey().longValue();

        return findOrderById(generatedId)
                .orElseThrow(() -> new IllegalStateException("保存した注文の取得に失敗しました"));
    }

    @Override
    public Optional<Order> findOrderById(Long orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, orderRowMapper, orderId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findAllOrdersByUserId(Long userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY ordered_at DESC";
        return jdbcTemplate.query(sql, orderRowMapper, userId);
    }

    @Override
    public Order updateOrder(Order order) {

        String sql = "UPDATE orders SET status = ?, total_price = ?, shipping_address = ?, " +
                "shipping_postal_code = ?, shipping_name = ?, ordered_at = ? WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getShippingAddress(),
                order.getPostalCode(),
                order.getShippingName(),
                Timestamp.valueOf(order.getOrderedAt()),
                order.getId()
        );

        if (rowsAffected == 0) {
            throw new ResourceNotFoundException("更新対象の注文が見つかりません。ID: " + order.getId());
        }

        return order;
    }

    @Override
    public List<OrderSummaryDto> findOrderSummariesByUserId(Long userId) {
        String sql = """
                SELECT
                    o.id,
                    o.ordered_at,
                    o.total_price,
                    o.status
                FROM
                    orders o
                WHERE
                    o.user_id = :userId
                ORDER BY
                    o.ordered_at DESC
                """;

        Map<String, Object> params = Map.of("userId", userId);

        RowMapper<OrderSummaryDto> rowMapper = (rs, rowNum) -> new OrderSummaryDto(
                rs.getLong("id"),
                rs.getTimestamp("ordered_at").toLocalDateTime(),
                rs.getInt("total_price"),
                OrderStatus.valueOf(rs.getString("status"))
        );

        return namedParameterJdbcTemplate.query(sql, params, rowMapper);
    }
}
