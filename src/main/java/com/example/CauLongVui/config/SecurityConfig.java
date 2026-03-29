package com.example.CauLongVui.config;

import com.example.CauLongVui.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Cho phép tất cả request — phân quyền xử lý phía client
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                // Trang login tùy chỉnh
                .loginPage("/auth/login.html")
                // Service xử lý thông tin user từ Google
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                // Handler sau khi đăng nhập thành công
                .successHandler(oAuth2SuccessHandler)
                // Handler khi đăng nhập thất bại
                .failureUrl("/auth/login.html?error=oauth2_failed")
            );
        return http.build();
    }
}
