package demo.having.domain.notification.entity;

import demo.having.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String type;

    @Lob
    private String message;

    private Long relatedResourceId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
