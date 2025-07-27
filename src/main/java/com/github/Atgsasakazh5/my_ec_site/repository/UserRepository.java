package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.dto.UserDto;
import com.github.Atgsasakazh5.my_ec_site.entity.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

interface UserDao {
    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}

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

    @Override
    public Optional<User> findByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try {
            // queryForObjectは結果が1件であることを期待するメソッド
            // 見つからない場合はEmptyResultDataAccessExceptionをスローする
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, name);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        // queryForObjectの方が、結果が0件または2件以上の場合に例外を投げてくれるため、より安全
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        // queryForObjectの引数の渡し方は、Object[]よりも直接渡す方がタイプセーフです
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public User save(User user) {
        String sql = "INSERT INTO users (name, email, password, address, subscribing_newsletter, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getPassword(),
                user.getAddress(), user.isSubscribingNewsletter());

        // IDを取得するために再度クエリを実行
        String findSql = "SELECT * FROM users WHERE email = ?";

        return jdbcTemplate.queryForObject(findSql, userRowMapper, user.getEmail());

    }
}