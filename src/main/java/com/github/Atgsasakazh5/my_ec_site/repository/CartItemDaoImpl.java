package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.dto.CartItemDto;
import com.github.Atgsasakazh5.my_ec_site.entity.CartItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class CartItemDaoImpl implements CartItemDao {

    private final JdbcTemplate jdbcTemplate;

    public CartItemDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CartItem> cartItemRowMapper = (rs, rowNum) ->
            new CartItem(rs.getLong("id"), rs.getLong("cart_id"),
                    rs.getLong("sku_id"), rs.getInt("quantity"));

    @Override
    public CartItem save(CartItem cartItem) {
        String sql = "INSERT INTO cart_items (cart_id, sku_id, quantity) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, cartItem.getCartId());
            ps.setLong(2, cartItem.getSkuId());
            ps.setInt(3, cartItem.getQuantity());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        cartItem.setId(id);
        return cartItem;
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        String sql = "SELECT * FROM cart_items WHERE id = ?";
        try {
            CartItem cartItem = jdbcTemplate.queryForObject(sql, cartItemRowMapper, id);
            return Optional.ofNullable(cartItem);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CartItem> findByCartIdAndSkuId(Long cartId, Long skuId) {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ? AND sku_id = ?";
        try {
            CartItem cartItem = jdbcTemplate.queryForObject(sql, cartItemRowMapper, cartId, skuId);
            return Optional.ofNullable(cartItem);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CartItem> findByCartId(Long cartId) {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ?";
        return jdbcTemplate.query(sql, cartItemRowMapper, cartId);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByCartId(Long cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        jdbcTemplate.update(sql, cartId);
    }

    @Override
    public CartItem update(CartItem cartItem) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        jdbcTemplate.update(sql, cartItem.getQuantity(), cartItem.getId());
        return cartItem;
    }

    @Override
    public List<CartItemDto> findDetailedItemsByCartId(Long cartId) {
        // inventoriesテーブルへのJOINを追加
        String sql = """
                SELECT
                    ci.id AS cart_item_id,
                    p.name AS product_name,
                    p.image_url,
                    s.id AS sku_id,
                    s.size,
                    s.color,
                    (p.price + s.extra_price) AS final_price,
                    ci.quantity,
                    i.quantity AS stock_quantity
                FROM
                    cart_items ci
                JOIN
                    skus s ON ci.sku_id = s.id
                JOIN
                    products p ON s.product_id = p.id
                JOIN
                    inventories i ON s.id = i.sku_id
                WHERE
                    ci.cart_id = ?
                """;

        RowMapper<CartItemDto> rowMapper = (rs, rowNum) -> new CartItemDto(
                rs.getLong("cart_item_id"),
                rs.getString("product_name"),
                rs.getString("image_url"),
                rs.getLong("sku_id"),
                rs.getString("size"),
                rs.getString("color"),
                rs.getInt("final_price"),
                rs.getInt("quantity"),
                rs.getInt("stock_quantity") // stock_quantityをマッピング
        );

        return jdbcTemplate.query(sql, rowMapper, cartId);
    }
}
