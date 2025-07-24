package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Role;
import com.github.Atgsasakazh5.my_ec_site.entity.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @DisplayName("ロール名でロールを検索できること")
    @Test
    void findByName_shouldReturnRole_whenNameExists() {
        // Arrange
        // テスト用のデータを保存
        roleRepository.save(new Role(null, RoleName.ROLE_USER));
        roleRepository.save(new Role(null, RoleName.ROLE_ADMIN));

        // Act
        // ROLE_USERで検索
        Optional<Role> foundRole = roleRepository.findByName(RoleName.ROLE_USER);

        // Assert
        assertThat(foundRole).isPresent(); // Optionalが空でないことを確認
        assertThat(foundRole.get().getName()).isEqualTo(RoleName.ROLE_USER); // 名前が正しいことを確認

    }

    @DisplayName("存在しないロール名で検索すると空のOptionalを返すこと")
    @Test
    void findByName_shouldReturnEmpty_whenNameNotExists() {
        // Arrange

        // Act
        // 存在しないロール名で検索
        Optional<Role> foundRole = roleRepository.findByName(RoleName.ROLE_ADMIN);

        // Assert
        assertThat(foundRole).isEmpty(); // Optionalが空であることを確認
    }

    @DisplayName("ロールを保存してIDを自動生成できること")
    @Test
    void save_shouldGenerateId_whenRoleIsSaved() {
        // Arrange
        Role role = new Role(null, RoleName.ROLE_USER);

        // Act
        Role savedRole = roleRepository.save(role);

        // Assert
        assertNotNull(savedRole.getId()); // IDが自動生成されていることを確認
        assertEquals(RoleName.ROLE_USER, savedRole.getName()); // 名前が正しいことを確認
    }
}