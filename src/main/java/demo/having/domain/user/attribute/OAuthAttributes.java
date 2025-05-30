package demo.having.domain.user.attribute;

import demo.having.domain.user.entity.Provider;
import demo.having.domain.user.entity.Role;
import demo.having.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String nickname;
    private String email;
    private String picture;
    private String providerId;
    private Provider provider; // String 대신 Provider Enum으로 변경

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String nickname, String email, String picture, String providerId, Provider provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.nickname = nickname;
        this.email = email;
        this.picture = picture;
        this.providerId = providerId;
        this.provider = provider; // 새로 추가된 필드
    }

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        Provider providerEnum = Provider.fromString(registrationId); // String -> Enum 변환
        switch (providerEnum) {
            case GOOGLE:
                return ofGoogle(attributes);
            case KAKAO:
                return ofKakao(attributes);
            default:
                throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        }
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nickname((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .providerId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey("sub")
                .provider(Provider.GOOGLE) // Provider 필드 추가
                .build();
    }

    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .nickname((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .picture((String) profile.get("profile_image_url"))
                .providerId(String.valueOf(attributes.get("id")))
                .attributes(attributes)
                .nameAttributeKey("id")
                .provider(Provider.KAKAO) // Provider 필드 추가
                .build();
    }

    // User 엔티티의 Role 필드 타입을 Role Enum으로 맞춤
    public User toEntity() { // provider 인자 제거 (OAuthAttributes 내에 provider 필드 존재)
        return User.builder()
                .nickname(nickname)
                .email(email)
                .profileImageUrl(picture)
                .provider(provider.name()) // Enum -> String
                .providerId(providerId)
                .role(Role.USER) // Role.USER Enum 값 사용
                .build();
    }
}
