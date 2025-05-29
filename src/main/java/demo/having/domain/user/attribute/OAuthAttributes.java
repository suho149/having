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

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String nickname, String email, String picture, String providerId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.nickname = nickname;
        this.email = email;
        this.picture = picture;
        this.providerId = providerId;
    }

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        switch (registrationId) {
            case "google":
                return ofGoogle(attributes);
            case "kakao":
                return ofKakao(attributes);
            default:
                throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
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
                .build();
    }

    public User toEntity(String provider) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .profileImageUrl(picture)
                .provider(String.valueOf(Provider.valueOf(provider.toUpperCase())))
                .providerId(providerId)
                .role(String.valueOf(Role.USER))
                .build();
    }
}
