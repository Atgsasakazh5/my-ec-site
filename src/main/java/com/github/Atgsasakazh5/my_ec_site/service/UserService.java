package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.SignUpRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UserDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Cart;
import com.github.Atgsasakazh5.my_ec_site.entity.RoleName;
import com.github.Atgsasakazh5.my_ec_site.entity.User;
import com.github.Atgsasakazh5.my_ec_site.repository.CartDao;
import com.github.Atgsasakazh5.my_ec_site.repository.RoleRepository;
import com.github.Atgsasakazh5.my_ec_site.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartDao cartDao;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository, CartDao cartDao) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cartDao = cartDao;
    }

    @Transactional
    public UserDto register(SignUpRequestDto signUpRequestDto) {
        //emailの重複チェック
        if (userRepository.existsByEmail(signUpRequestDto.email())) {
            throw new IllegalStateException("メールアドレスはすでに使用されています");
        }

        //Roleの取得
        var role = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーロールが見つかりません"));

        // パスワードをハッシュ化
        String hashedPassword = passwordEncoder.encode(signUpRequestDto.password());

        // ユーザーの作成
        User user = new User();
        user.setName(signUpRequestDto.name());
        user.setEmail(signUpRequestDto.email());
        user.setPassword(hashedPassword);
        user.setAddress(signUpRequestDto.address());
        user.setSubscribingNewsletter(signUpRequestDto.subscribingNewsletter());
        user.setRoles(Set.of(role)); // ユーザーにロールを追加

        User savedUser = userRepository.save(user);
        cartDao.saveCart(savedUser.getId());

        return new UserDto(savedUser.getId(),
                           savedUser.getName(),
                           savedUser.getEmail());
    }

    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
