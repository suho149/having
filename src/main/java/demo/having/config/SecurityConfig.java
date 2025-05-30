package demo.having.config;

import demo.having.domain.user.handler.OAuth2AuthenticationFailureHandler;
import demo.having.domain.user.handler.OAuth2AuthenticationSuccessHandler;
import demo.having.domain.user.service.CustomOAuth2UserService;
import demo.having.domain.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler; // 필드명 변경
    private final AuthenticationFailureHandler oAuth2AuthenticationFailureHandler; // 필드명 변경
    private final CustomUserDetailsService customUserDetailsService; // 일반 로그인 사용 시

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 공개 접근 경로
                        .requestMatchers("/", "/login", "/login/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/study-groups", "/study-groups/{id:[0-9]+}").permitAll()

                        // 관리자 경로 (Role Enum의 key 사용)
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Role.ADMIN.getKey()는 "ROLE_ADMIN"이므로 hasRole은 "ADMIN"만 씀

                        // 인증이 필요한 경로
                        .requestMatchers("/dashboard", "/my-studies", "/schedule", "/messages", "/profile").authenticated()
                        .requestMatchers("/study-groups/new").authenticated()
                        .requestMatchers(HttpMethod.POST, "/study-groups").authenticated()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler) // 필드명 변경
                        .failureHandler(oAuth2AuthenticationFailureHandler) // 필드명 변경
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // 일반 로그인 (ID/PW)을 위해 UserDetailsService를 사용한다면
                .userDetailsService(customUserDetailsService); // 주석 처리 또는 제거 가능 (순수 OAuth2만 할 경우)

        return http.build();
    }

    // SecurityConfig 내에 Bean으로 직접 선언하여 주입받을 수 있도록 변경
    @Bean
    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler();
    }

    @Bean
    public AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler();
    }
}