package demo.having.domain.study.entity;

public enum StudyMemberStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    BANNED("차단됨");

    private final String displayName;

    StudyMemberStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
