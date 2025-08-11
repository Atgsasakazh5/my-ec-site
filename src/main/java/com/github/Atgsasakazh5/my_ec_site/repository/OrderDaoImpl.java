package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Order;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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

    public OrderDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) ->
            new Order(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("status"),
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
            var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, order.getUserId());
            ps.setString(2, order.getStatus());
            ps.setInt(3, order.getTotalPrice());
            ps.setString(4, order.getShippingAddress());
            ps.setString(5, order.getPostalCode());
            ps.setString(6, order.getShippingName());
            ps.setTimestamp(7, Timestamp.valueOf(order.getOrderedAt()));
            return ps;
        }, keyHolder);
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            order.setId(((Number) keys.get("id")).longValue());
        } else {
            throw new IllegalStateException("データベースから生成されたIDの取得に失敗しました。");
        }
        return order;
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
}
