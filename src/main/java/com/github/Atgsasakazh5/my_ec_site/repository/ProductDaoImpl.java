package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Product;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductDaoImpl implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) ->
            new Product(rs.getLong("id"), rs.getString("name"), rs.getInt("price"),
                    rs.getString("description"), rs.getString("image_url"),
                    rs.getInt("category_id"), rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime());

    @Override
    public Product save(Product product) {
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        String sql = "INSERT INTO products (name, price, description, image_url, category_id, created_At, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getName());
            ps.setInt(2, product.getPrice());
            ps.setString(3, product.getDescription());
            ps.setString(4, product.getImageUrl());
            ps.setInt(5, product.getCategoryId());
            ps.setTimestamp(6, Timestamp.valueOf(product.getCreatedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(product.getUpdatedAt()));
            return ps;
        }, keyHolder);

        product.setId(keyHolder.getKey().longValue());

        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {

        String sql = "SELECT * FROM products WHERE id = ?";
        try {
            Product product = jdbcTemplate.queryForObject(sql, productRowMapper, id);
            return Optional.ofNullable(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM products";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    @Override
    public Product update(Product product) {
        String sql = "UPDATE products SET name = ?, price = ?, description = ?, image_url = ?, " +
                "category_id = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, product.getName(), product.getPrice(), product.getDescription(),
                product.getImageUrl(), product.getCategoryId(),
                Timestamp.valueOf(product.getUpdatedAt()), product.getId());
        return product;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        jdbcTemplate.update(sql, id);

    }

    @Override
    public List<Product> findAll(int page, int size) {
        // OFFSETを計算
        int offset = page * size;
        String sql = "SELECT * FROM products ORDER BY id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, productRowMapper, size, offset);
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM products";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return (count != null) ? count : 0;
    }

    @Override
    public List<Product> findByCategoryId(int categoryId, int page, int size) {
        String sql = "SELECT * FROM products WHERE category_id = ? ORDER BY id DESC LIMIT ? OFFSET ?";
        try {
            return jdbcTemplate.query(sql, productRowMapper, categoryId, size, page * size);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public int countByCategoryId(int categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, categoryId);
        return (count != null) ? count : 0;
    }
}
