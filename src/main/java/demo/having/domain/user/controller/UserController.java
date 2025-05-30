package demo.having.domain.user.controller;

import demo.having.domain.user.entity.CustomOAuth2User;
import demo.having.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor // UserService 주입받을 경우
public class UserController {

    private final UserService userService; // 필요 시

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomOAuth2User principal) { // CustomOAuth2User로 타입 지정
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // CustomOAuth2User에서 직접 User 엔티티를 가져와 필요한 정보를 반환
        return ResponseEntity.ok(Map.of(
                "userId", principal.getUser().getUserId(),
                "email", principal.getUser().getEmail(),
                "nickname", principal.getUser().getNickname(),
                "profileImageUrl", principal.getUser().getProfileImageUrl(),
                "role", principal.getUser().getRole().getKey() // 역할도 포함
        ));
    }
}