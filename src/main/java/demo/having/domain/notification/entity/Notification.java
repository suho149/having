package demo.having.domain.notification.entity;

import demo.having.domain.baseentity.BaseTimeEntity;
import demo.having.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Notification extends BaseTimeEntity { // BaseTimeEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // Enum 사용

    @Lob
    @Column(nullable = false)
    private String message;

    // 관련 리소스 타입과 ID를 함께 저장하여 유연성 확보
    private String relatedResourceType; // 예: "StudyGroup", "ChatMessage", "Schedule"
    private Long relatedResourceId;

    @Column(nullable = false)
    private Boolean isRead; // 기본값 false 설정

    @Builder
    public Notification(User user, NotificationType type, String message, String relatedResourceType, Long relatedResourceId) {
        this.user = user;
        this.type = type;
        this.message = message;
        this.relatedResourceType = relatedResourceType;
        this.relatedResourceId = relatedResourceId;
        this.isRead = false; // 기본값 false
    }

    // 읽음 상태 변경 메서드
    public void markAsRead() {
        this.isRead = true;
    }
}
