package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InventoryDaoImpl implements InventoryDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public InventoryDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<Inventory> inventoryRowMapper = (rs, rowNum) ->
            new Inventory(rs.getLong("id"),
                    rs.getLong("sku_id"),
                    rs.getInt("quantity"),
                    rs.getTimestamp("updated_at").toLocalDateTime());

    @Override
    public Inventory save(Inventory inventory) {
        var now = LocalDateTime.now();
        inventory.setUpdatedAt(now);

        String sql = "INSERT INTO inventories (sku_id, quantity, updated_at) " +
                "VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, inventory.getSkuId());
            ps.setInt(2, inventory.getQuantity());
            ps.setTimestamp(3, Timestamp.valueOf(inventory.getUpdatedAt()));
            return ps;
        }, keyHolder);
        inventory.setId(keyHolder.getKey().longValue());
        return inventory;
    }

    @Override
    public Inventory update(Inventory inventory) {
        String sql = "UPDATE inventories SET sku_id = ?, quantity = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, inventory.getSkuId(), inventory.getQuantity(),
                Timestamp.valueOf(inventory.getUpdatedAt()), inventory.getId());
        return inventory;
    }

    @Override
    public Optional<Inventory> findBySkuId(Long skuId) {
        String sql = "SELECT * FROM inventories WHERE sku_id = ?";
        try {
            Inventory inventory = jdbcTemplate.queryForObject(sql, inventoryRowMapper, skuId);
            return Optional.ofNullable(inventory);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        String sql = "SELECT * FROM inventories WHERE id = ?";
        try {
            Inventory inventory = jdbcTemplate.queryForObject(sql, inventoryRowMapper, id);
            return Optional.ofNullable(inventory);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteBySkuId(Long skuId) {
        String sql = "DELETE FROM inventories WHERE sku_id = ?";
        jdbcTemplate.update(sql, skuId);
    }

    @Override
    public void deleteBySkuIds(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        String sql = "DELETE FROM inventories WHERE sku_id IN (:skuIds)";
        Map<String, List<Long>> params = Map.of("skuIds", skuIds);
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public List<Inventory> findBySkuIdIn(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        String sql = "SELECT * FROM inventories WHERE sku_id IN (:skuIds)";
        // NamedParameterJdbcTemplateを使うとIN句を簡単に扱える
        Map<String, List<Long>> params = Map.of("skuIds", skuIds);
        return namedParameterJdbcTemplate.query(sql, params, inventoryRowMapper);
    }
}
