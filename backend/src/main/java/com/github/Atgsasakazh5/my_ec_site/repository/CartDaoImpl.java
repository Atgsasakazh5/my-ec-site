package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Cart;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class CartDaoImpl implements CartDao {

    private final JdbcTemplate jdbcTemplate;

    public CartDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Cart saveCart(Long userId) {

        String sql = "INSERT INTO carts (user_id) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            return ps;
        }, keyHolder);

        Long cartId = keyHolder.getKey().longValue();
        return new Cart(cartId, userId);
    }

    @Override
    public Optional<Cart> findCartByUserId(Long userId) {
        String sql = "SELECT * FROM carts WHERE user_id = ?";
        try {
            Cart cart = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new Cart(rs.getLong("id"), rs.getLong("user_id")), userId);
            return Optional.ofNullable(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Cart> findCartByEmail(String email) {
        String sql = "SELECT c.* FROM carts c JOIN users u ON c.user_id = u.id WHERE u.email = ?";
        try {
            Cart cart = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new Cart(rs.getLong("id"), rs.getLong("user_id")), email);
            return Optional.ofNullable(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
