package demo.having.domain.study.entity;

import demo.having.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class StudyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;

    private LocalDateTime joinDate; // 가입일은 그대로 유지하거나 createdAt으로 통일
    // @CreatedDate, @LastModifiedDate 사용을 위해 BaseTimeEntity 상속 고려

    @Enumerated(EnumType.STRING)
    private StudyRole roleInStudy; // Enum 사용

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus; // Enum 사용

    private boolean isLeader; // 이 멤버가 스터디의 리더인지 여부 (StudyGroup 엔티티의 leader 필드와 연계)

    // 편의 메서드: 승인 상태 변경
    public void updateApprovalStatus(ApprovalStatus status) {
        this.approvalStatus = status;
    }

    // 편의 메서드: 스터디 내 역할 변경
    public void updateRoleInStudy(StudyRole role) {
        this.roleInStudy = role;
    }
}