package com.example.CauLongVui.config;

import com.example.CauLongVui.entity.User;
import com.example.CauLongVui.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // Lấy user từ DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại: " + email));

        if (!user.getActive()) {
            log.warn("Tài khoản bị vô hiệu hóa: {}", email);
            response.sendRedirect("/auth/login.html?error=account_disabled");
            return;
        }

        log.info("OAuth2 login thành công: {} (role={})", email, user.getRole());

        // Redirect về trang callback, truyền userId để frontend lấy thông tin
        String redirectUrl = "/auth/oauth2-callback.html?userId=" + user.getId();
        response.sendRedirect(redirectUrl);
    }
}
