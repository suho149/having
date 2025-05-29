package demo.having.domain.user.controller;

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
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 커스텀 OAuth2User를 썼다면 캐스팅 필요
        return ResponseEntity.ok(Map.of(
                "email", principal.getAttribute("email"),
                "nickname", principal.getAttribute("name"), // 혹은 별도로 저장한 닉네임
                "profileImageUrl", principal.getAttribute("picture") // 구글은 picture, 카카오는 profile_image
        ));
    }
}
