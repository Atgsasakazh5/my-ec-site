package com.github.Atgsasakazh5.my_ec_site.config;

import com.github.Atgsasakazh5.my_ec_site.security.JwtAuthFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoderを使用してパスワードをハッシュ化
        return new BCryptPasswordEncoder();
    }

//    public static void main(String[] args) {
//        String password = "password123";
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String hashedPassword = passwordEncoder.encode(password);
//        System.out.println("Hashed Password: " + hashedPassword);
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .cors(withDefaults())
                // CSRF保護を無効化
                .csrf(AbstractHttpConfigurer::disable)
                // JWT認証フィルターを適用
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // セッション管理の設定を追加
                .sessionManagement(sessionManager -> sessionManager

                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // HTTPリクエストに対する認可設定
                .authorizeHttpRequests(auth -> auth
                        // 認証不要でリクエストを許可するパスを指定
                        .requestMatchers("/api/auth/**", "/api/products/**", "/api/categories/**").permitAll()
                        // "/api/admin/**"へのリクエストはADMINロールを持つユーザーのみ許可
                        .requestMatchers(
                                "/api/users/me",
                                "/api/cart/**",
                                "/api/orders/**"
                        ).authenticated()
                        // 上記以外はすべて拒否
                        .anyRequest().denyAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // 認証マネージャーを提供
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // フロントエンドのURLを許可
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        // 許可するHTTPメソッド
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 許可するヘッダー
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
