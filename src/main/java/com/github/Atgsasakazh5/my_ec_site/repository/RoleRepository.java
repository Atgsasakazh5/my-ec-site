package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Role;
import com.github.Atgsasakazh5.my_ec_site.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * ロール名でロールを検索するメソッド
     * @param name 検索するロール名 (RoleName Enum)
     * @return 見つかったロールエンティティを格納したOptional
     */
    Optional<Role> findByName(RoleName name);
}
