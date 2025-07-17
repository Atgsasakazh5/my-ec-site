package com.github.Atgsasakazh5.my_ec_site.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoderを使用してパスワードをハッシュ化
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF保護を無効化
                .csrf(AbstractHttpConfigurer::disable)
                // セッション管理の設定を追加
                .sessionManagement(sessionManager -> sessionManager

                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // HTTPリクエストに対する認可設定
                .authorizeHttpRequests(auth -> auth
                        // "/api/auth/**"へのリクエストは全て許可 (認証不要)
                        .requestMatchers("/api/auth/**").permitAll()
                        // 上記以外のリクエストは全て認証が必要
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // 認証マネージャーを提供
        return authenticationConfiguration.getAuthenticationManager();
    }
}
