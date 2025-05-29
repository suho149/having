package demo.having.domain.user.entity;

public enum Provider {
    LOCAL,    // 자체 회원가입
    GOOGLE,   // 구글 OAuth2
    KAKAO,    // 카카오 OAuth2
    NAVER     // 네이버 OAuth2 (추후 확장 시)
}
