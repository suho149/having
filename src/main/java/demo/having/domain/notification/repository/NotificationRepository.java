package demo.having.domain.notification.repository;

import demo.having.domain.notification.entity.Notification;
import demo.having.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 사용자의 최신 알림 N개를 가져오는 메서드
    List<Notification> findTop5ByUserOrderByCreatedAtDesc(User user);

    // 읽지 않은 알림 수
    long countByUserAndIsReadFalse(User user);
}
