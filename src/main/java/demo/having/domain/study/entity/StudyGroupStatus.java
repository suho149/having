package demo.having.domain.study.entity;

public enum StudyGroupStatus {
    RECRUITING("모집 중"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료됨"),
    CANCELLED("취소됨");

    private final String displayName;

    StudyGroupStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
