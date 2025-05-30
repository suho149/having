package demo.having.domain.study.entity;

public enum LocationType {
    ONLINE("온라인"),
    OFFLINE("오프라인"),
    HYBRID("온/오프라인 병행");

    private final String displayName; // displayName 필드 추가

    LocationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { // public getter 메서드 추가
        return displayName;
    }
}
