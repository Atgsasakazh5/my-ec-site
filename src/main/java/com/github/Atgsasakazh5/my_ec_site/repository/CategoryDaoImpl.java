package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Category;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@Repository
public class CategoryDaoImpl implements CategoryDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CategoryDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<Category> categoryRowMapper = (rs, rowNum) ->
            new Category(rs.getInt("id"), rs.getString("name"));
    ;

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories order by id";

        return jdbcTemplate.query(sql, categoryRowMapper);

    }

    @Override
    public Category update(Integer id, String name) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";

        jdbcTemplate.update(sql, name, id);
        return new Category(id, name);

    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        jdbcTemplate.update(sql, id);

    }

    @Override
    public Category save(String name) {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            return ps;
        }, keyHolder);

        Integer generatedId = keyHolder.getKey().intValue();
        return new Category(generatedId, name);

    }

    @Override
    public Optional<Category> findById(Integer id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try {
            Category category = jdbcTemplate.queryForObject(sql, categoryRowMapper, id);
            return Optional.ofNullable(category);
        } catch (EmptyResultDataAccessException e) {
            // カテゴリが見つからなかった場合はemptyを返す
            return Optional.empty();
        }
    }

    @Override
    public Optional<Category> findByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";
        try {
            Category category = jdbcTemplate.queryForObject(sql, categoryRowMapper, name);
            return Optional.ofNullable(category);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Category> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String sql = "SELECT * FROM categories WHERE id IN (:ids)";
        // NamedParameterJdbcTemplateを使うとIN句を簡単に扱える
        Map<String, List<Integer>> params = java.util.Map.of("ids", ids);
        return namedParameterJdbcTemplate.query(sql, params, categoryRowMapper);
    }
}
