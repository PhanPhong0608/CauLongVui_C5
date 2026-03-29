package com.example.CauLongVui.service;

import com.example.CauLongVui.entity.User;
import com.example.CauLongVui.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Lấy thông tin từ Google
        String email      = oAuth2User.getAttribute("email");
        String name       = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");   // Google unique ID
        String avatarUrl  = oAuth2User.getAttribute("picture");

        log.info("Google OAuth2 login: email={}, name={}", email, name);

        // Tìm user theo email, nếu chưa có thì tạo mới
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            // Tạo user mới từ Google account
            User newUser = User.builder()
                    .fullName(name != null ? name : email)
                    .email(email)
                    .password(null)                 // Google user không có password
                    .provider(User.Provider.GOOGLE)
                    .providerId(providerId)
                    .avatarUrl(avatarUrl)
                    .role(User.Role.CUSTOMER)
                    .active(true)
                    .build();
            userRepository.save(newUser);
            log.info("Tạo user mới từ Google: {}", email);
        } else {
            // Cập nhật thông tin từ Google (avatar, providerId) nếu cần
            User user = existingUser.get();
            boolean updated = false;

            if (avatarUrl != null && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
                updated = true;
            }
            if (user.getProviderId() == null) {
                user.setProviderId(providerId);
                user.setProvider(User.Provider.GOOGLE);
                updated = true;
            }
            if (updated) {
                userRepository.save(user);
            }
        }

        return oAuth2User;
    }
}
