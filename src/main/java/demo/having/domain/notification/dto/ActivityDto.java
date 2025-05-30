package demo.having.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ActivityDto {

    private String iconClass;
    private String description;
    private String timeAgo;
    private Long notificationId; // 알림 ID (클릭 시 상세 페이지로 이동 등에 활용)
}
