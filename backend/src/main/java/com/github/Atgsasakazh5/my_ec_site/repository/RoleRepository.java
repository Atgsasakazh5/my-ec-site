package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Role;
import com.github.Atgsasakazh5.my_ec_site.entity.RoleName;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

interface RoleDao {
    /**
     * ロール名でロールを検索するメソッド
     *
     * @param name 検索するロール名 (RoleName Enum)
     * @return 見つかったロールエンティティを格納したOptional
     */
    Optional<Role> findByName(RoleName name);

    Role save(Role role);
}

@Repository
public class RoleRepository implements RoleDao {

    private final JdbcTemplate jdbcTemplate;

    public RoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * ロール名でロールを検索するメソッド
     *
     * @param name 検索するロール名 (RoleName Enum)
     * @return 見つかったロールエンティティを格納したOptional
     */
    public Optional<Role> findByName(RoleName name) {
        String sql = "SELECT * FROM roles WHERE name = ?";
        try {
            Role role = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                return new Role(rs.getInt("id"), RoleName.valueOf(rs.getString("name")));
            }, name.name());
            return Optional.ofNullable(role);
        } catch (EmptyResultDataAccessException e) { // <- 具体的な例外をキャッチ
            return Optional.empty();
        }
    }

    /**
     * ロールを保存するメソッド
     *
     * @param role 保存するロールエンティティ
     */
    @Override
    public Role save(Role role) {
        String sql = "INSERT INTO roles (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // ラムダ式を使って、SQLと生成キーの取得設定を持つPreparedStatementを作成
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, role.getName().name());
            return ps;
        }, keyHolder);

        // 生成されたIDを取得
        int generatedId = keyHolder.getKey().intValue();

        // セッターがないので、IDを含む新しいインスタ-ンスを生成して返す
        return new Role(generatedId, role.getName());
    }

}
