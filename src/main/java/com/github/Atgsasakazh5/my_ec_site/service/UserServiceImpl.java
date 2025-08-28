package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.SignUpRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UserDto;
import com.github.Atgsasakazh5.my_ec_site.entity.RoleName;
import com.github.Atgsasakazh5.my_ec_site.entity.User;
import com.github.Atgsasakazh5.my_ec_site.repository.CartDao;
import com.github.Atgsasakazh5.my_ec_site.repository.RoleRepository;
import com.github.Atgsasakazh5.my_ec_site.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartDao cartDao;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository, CartDao cartDao) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cartDao = cartDao;
    }

    @Override
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
        user.setRoles(Set.of(role));

        User savedUser = userRepository.save(user);
        cartDao.saveCart(savedUser.getId());

        Set<String> roles = savedUser.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return new UserDto(savedUser.getId(),
                           savedUser.getName(),
                           savedUser.getEmail(),
                           roles);
    }

    @Override
    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new UserDto(user.getId(), user.getName(), user.getEmail(), roles);
    }
}
