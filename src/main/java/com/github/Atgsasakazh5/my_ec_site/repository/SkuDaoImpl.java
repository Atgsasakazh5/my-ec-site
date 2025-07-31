package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Sku;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class SkuDaoImpl implements SkuDao{

    private final JdbcTemplate jdbcTemplate;

    public SkuDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Sku> skuRowMapper = (rs, rowNum) ->
            new Sku(rs.getLong("id"), rs.getLong("product_id"),
                    rs.getString("size"),
                    rs.getString("color"), rs.getInt("extra_price"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime());

    @Override
    public Sku save(Sku sku) {
        String sql = "INSERT INTO skus (product_id, size, color, extra_price, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, sku.getProductId());
            ps.setString(2, sku.getSize());
            ps.setString(3, sku.getColor());
            ps.setInt(4, sku.getExtraPrice());
            ps.setTimestamp(5, Timestamp.valueOf(sku.getCreatedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(sku.getUpdatedAt()));
            return ps;
        }, keyHolder);
        sku.setId(keyHolder.getKey().longValue());
        return sku;
    }

    @Override
    public Optional<Sku> findById(Long id) {
        String sql = "SELECT * FROM skus WHERE id = ?";
        try {
            Sku sku = jdbcTemplate.queryForObject(sql, skuRowMapper, id);
            return Optional.ofNullable(sku);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Sku> findByProductId(Long productId) {
        String sql = "SELECT * FROM skus WHERE product_id = ?";
        return jdbcTemplate.query(sql, skuRowMapper, productId);
    }

    @Override
    public Sku update(Sku sku) {
        String sql = "UPDATE skus SET product_id = ?, size = ?, color = ?, extra_price = ?, " +
                     "updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, sku.getProductId(), sku.getSize(), sku.getColor(),
                sku.getExtraPrice(), Timestamp.valueOf(sku.getUpdatedAt()), sku.getId());
        return sku;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM skus WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Sku> findByProductIdAndSizeAndColor(Long productId, String size, String color) {
        String sql = "SELECT * FROM skus WHERE product_id = ? AND size = ? AND color = ?";
        try {
            Sku sku = jdbcTemplate.queryForObject(sql, skuRowMapper, productId, size, color);
            return Optional.ofNullable(sku);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
