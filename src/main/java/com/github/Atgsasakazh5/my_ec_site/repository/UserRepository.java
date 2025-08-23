package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.dto.UserDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Role;
import com.github.Atgsasakazh5.my_ec_site.entity.RoleName;
import com.github.Atgsasakazh5.my_ec_site.entity.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserRepository implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 再利用可能なRowMapperを定義
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setAddress(rs.getString("address"));
        user.setSubscribingNewsletter(rs.getBoolean("subscribing_newsletter"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        // rolesは別途取得する必要がある
        return user;
    };

    private Set<Role> findRolesByUserId(Long userId) {
        String sql = "SELECT r.id, r.name FROM roles r " +
                "JOIN user_roles ur ON r.id = ur.role_id " +
                "WHERE ur.user_id = ?";

        RowMapper<Role> roleRowMapper = (rs, rowNum) -> new Role(
                rs.getInt("id"),
                RoleName.valueOf(rs.getString("name"))
        );

        return new HashSet<>(jdbcTemplate.query(sql, roleRowMapper, userId));
    }

    @Override
    public Optional<User> findByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, name);
            if (user != null) {
                user.setRoles(findRolesByUserId(user.getId()));
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            if (user != null) {
                // ユーザーが見つかったら、ロール情報を取得してセットする
                user.setRoles(findRolesByUserId(user.getId()));
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (name, email, password, address, subscribing_newsletter, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";

        // 生成されたキー（ID）を格納するKeyHolderを準備
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // KeyHolderを使ってIDを取得できるupdateメソッドを呼び出す
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getAddress());
            ps.setBoolean(5, user.isSubscribingNewsletter());
            return ps;
        }, keyHolder);

        // KeyHolderから生成されたIDを取得して、元のUserオブジェクトに設定する
        // UserエンティティにsetId(Long id)メソッドがあることを想定しています
        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().longValue());
        }

        return user;
    }
}