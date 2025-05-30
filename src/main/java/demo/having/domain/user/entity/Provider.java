package demo.having.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    GOOGLE,
    KAKAO;

    // String 값을 Enum으로 변환할 때 사용할 수 있는 정적 팩토리 메서드
    public static Provider fromString(String text) {
        for (Provider p : Provider.values()) {
            if (p.name().equalsIgnoreCase(text)) {
                return p;
            }
        }
        throw new IllegalArgumentException("No enum constant for provider: " + text);
    }
}
