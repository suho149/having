package demo.having.domain.notification.service;

import demo.having.domain.notification.entity.Notification;
import demo.having.domain.notification.entity.NotificationType;
import demo.having.domain.notification.repository.NotificationRepository;
import demo.having.domain.study.entity.StudyGroup;
import demo.having.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createStudyGroupCreationNotification(User user, StudyGroup studyGroup) {
        String message = studyGroup.getName(); // 스터디 이름만 메시지로 사용
        Notification notification = Notification.builder()
                .user(user) // 알림을 받을 사용자 (여기서는 스터디 생성자)
                .type(NotificationType.NEW_STUDY_CREATED)
                .message(message)
                .relatedResourceType("StudyGroup")
                .relatedResourceId(studyGroup.getStudyGroupId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    // 다른 알림 생성 메서드 (예: 메시지 알림, 일정 알림 등)는 여기에 추가
}
