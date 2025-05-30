package demo.having.domain.user.entity;

import demo.having.domain.baseentity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// @Builder // BaseTimeEntity 상속 시 Builder 패턴 사용에 주의 필요
// @AllArgsConstructor // BaseTimeEntity 상속 시 AllArgsConstructor 사용에 주의 필요
// @Builder는 일반적으로 모든 필드를 포함하는 생성자를 만드므로, createdAt/updatedAt을 수동으로 설정할 수 없게 됨
// 대신, 필요한 필드만 포함하는 생성자를 만들고 Builder를 사용
public class User extends BaseTimeEntity { // BaseTimeEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    private String password; // 일반 로그인 사용 시 유지

    @Column(nullable = false, unique = true)
    private String nickname;

    private String profileImageUrl;

    @Lob
    private String introduction;

    @Enumerated(EnumType.STRING) // Enum 사용
    @Column(nullable = false)
    private Role role; // Role Enum 타입

    @Column(nullable = false)
    private String provider;

    private String providerId;

    private LocalDateTime deletedAt; // BaseTimeEntity에 없으므로 여기에 유지

    // Builder 패턴을 유지하면서 생성자를 직접 정의하여 BaseTimeEntity 필드 제외
    @Builder
    public User(Long userId, String email, String password, String nickname, String profileImageUrl, String introduction, String provider, String providerId, Role role) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }

    // OAuth2 로그인 시 사용자 정보 업데이트 메서드 (Auditing으로 updatedAt 자동 관리)
    public void update(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    // Role 업데이트 메서드 (필요시)
    public void setRole(Role role) {
        this.role = role;
    }
}
