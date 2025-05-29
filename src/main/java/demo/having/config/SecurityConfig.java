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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;
    private final CustomUserDetailsService customUserDetailsService;

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

                        // 관리자 경로
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 로그아웃 엔드포인트는 인증된 사용자가 접근할 수 있도록 명시 (또는 permitAll() 후 서비스단에서 처리)
                        // .requestMatchers("/logout").authenticated() // 아래 logout 설정에서 permitAll()로 처리했으므로 여기선 불필요할 수 있음

                        // 🚨 인증이 필요한 경로 (문제의 경로 포함)
                        .requestMatchers("/dashboard", "/my-studies", "/schedule", "/messages", "/profile").authenticated()
                        .requestMatchers("/study-groups/new").authenticated() // "새 스터디 그룹 만들기" 폼 접근
                        .requestMatchers(HttpMethod.POST, "/study-groups").authenticated() // 폼 제출

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll() // 로그아웃 처리는 모두에게 허용
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }
}