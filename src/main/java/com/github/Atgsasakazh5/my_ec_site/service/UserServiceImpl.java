package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.SignUpRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UserDto;
import com.github.Atgsasakazh5.my_ec_site.entity.RoleName;
import com.github.Atgsasakazh5.my_ec_site.entity.User;
import com.github.Atgsasakazh5.my_ec_site.entity.VerificationToken;
import com.github.Atgsasakazh5.my_ec_site.repository.CartDao;
import com.github.Atgsasakazh5.my_ec_site.repository.RoleRepository;
import com.github.Atgsasakazh5.my_ec_site.repository.UserRepository;
import com.github.Atgsasakazh5.my_ec_site.repository.VerificationTokenDao;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartDao cartDao;
    private final VerificationTokenDao verificationTokenDao;
    private final EmailService emailService;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository, CartDao cartDao, VerificationTokenDao verificationTokenDao, EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cartDao = cartDao;
        this.verificationTokenDao = verificationTokenDao;
        this.emailService = emailService;
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
        user.setEmailVerified(false);
        user.setSubscribingNewsletter(signUpRequestDto.subscribingNewsletter());
        user.setRoles(Set.of(role));

        User savedUser = userRepository.save(user);
        cartDao.saveCart(savedUser.getId());

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserId(savedUser.getId());
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenDao.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), token);

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

    @Override
    @Transactional
    public void verifyUser(String token) {
        VerificationToken verificationToken = verificationTokenDao.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効なトークンです"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("トークンの有効期限が切れています");
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        user.setEmailVerified(true);
        userRepository.update(user);

        verificationTokenDao.delete(verificationToken);
    }

    @Override
    @Transactional
    public void createAdminUserIfNotFound(String email, String password) {
        if (!userRepository.existsByEmail(email)) {
            var adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("管理者ロールが見つかりません"));
            var userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new IllegalStateException("ユーザーロールが見つかりません"));

            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setAddress("Admin Address");
            admin.setEmailVerified(true);
            admin.setSubscribingNewsletter(false);
            admin.setRoles(Set.of(adminRole, userRole));

            userRepository.save(admin);
        }
    }
}
