package demo.having.domain.user.repository;

import demo.having.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회 (OAuth2, 일반 로그인 공통 사용)
    Optional<User> findByEmail(String email);

    // provider + providerId 로 사용자 조회 (OAuth2 전용)
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Optional<User> findByNickname(String nickname);

    boolean existsByEmail(String email);

    // 닉네임 중복 검사
    boolean existsByNickname(String nickname);
}
