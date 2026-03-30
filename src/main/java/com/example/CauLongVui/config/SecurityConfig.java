package com.example.CauLongVui.config;

import com.example.CauLongVui.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    // Buộc Google luôn hiện màn hình chọn tài khoản
    private OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params -> params.put("prompt", "select_account")));
        return resolver;
    }

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
                // Resolver buộc hiện màn hình chọn tài khoản Google
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestResolver(customAuthorizationRequestResolver())
                )
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
