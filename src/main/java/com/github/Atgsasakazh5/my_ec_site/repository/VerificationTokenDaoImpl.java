package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class VerificationTokenDaoImpl implements VerificationTokenDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public VerificationTokenDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final class VerificationTokenRowMapper implements RowMapper<VerificationToken> {
        @Override
        public VerificationToken mapRow(ResultSet rs, int rowNum) throws SQLException {
            VerificationToken token = new VerificationToken();
            token.setId(rs.getLong("id"));
            token.setToken(rs.getString("token"));
            token.setUserId(rs.getLong("user_id"));
            token.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
            return token;
        }
    }

    @Override
    public void save(VerificationToken token) {
        String sql = "INSERT INTO verification_tokens (token, user_id, expiry_date) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, token.getToken(), token.getUserId(), Timestamp.valueOf(token.getExpiryDate()));
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        String sql = "SELECT * FROM verification_tokens WHERE token = ?";
        return jdbcTemplate.query(sql, new VerificationTokenRowMapper(), token).stream().findFirst();
    }

    @Override
    public void delete(VerificationToken token) {
        String sql = "DELETE FROM verification_tokens WHERE id = ?";
        jdbcTemplate.update(sql, token.getId());
    }
}
