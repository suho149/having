package demo.having.domain.user.controller;

import demo.having.domain.user.entity.CustomOAuth2User;
import demo.having.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            // 로그인된 사용자
            if (principal instanceof CustomOAuth2User) {
                CustomOAuth2User customUser = (CustomOAuth2User) principal;
                User user = customUser.getUser();
                model.addAttribute("user", user);
                model.addAttribute("isLoggedIn", true);
            }
        } else {
            // 비로그인 사용자
            model.addAttribute("isLoggedIn", false);
        }

        return "index";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User customUser = (CustomOAuth2User) principal;
            User user = customUser.getUser();
            model.addAttribute("user", user);

            log.info("로그인한 사용자: {}, 이메일: {}, 제공자: {}",
                    user.getNickname(), user.getEmail(), user.getProvider());
        }

        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User customUser = (CustomOAuth2User) principal;
            User user = customUser.getUser();
            model.addAttribute("user", user);
        }

        return "profile";
    }
}
