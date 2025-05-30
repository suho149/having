package demo.having.domain.user.controller;

import demo.having.domain.notification.dto.ActivityDto;
import demo.having.domain.notification.entity.Notification;
import demo.having.domain.notification.repository.NotificationRepository;
import demo.having.domain.study.entity.StudyGroup;
import demo.having.domain.study.entity.StudyMember;
import demo.having.domain.study.repository.StudyMemberRepository;
import demo.having.domain.user.entity.CustomOAuth2User;
import demo.having.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final StudyMemberRepository studyMemberRepository;
    private final NotificationRepository notificationRepository;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal CustomOAuth2User principal) { // 타입 캐스팅 필요 없음
        if (principal != null) {
            // 로그인된 사용자
            User user = principal.getUser();
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
        } else {
            // 비로그인 사용자
            model.addAttribute("isLoggedIn", false);
        }
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() { // Model은 필요 없음
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal CustomOAuth2User principal) {
        if (principal != null && principal.getUser() != null) {
            User user = principal.getUser();
            model.addAttribute("user", user);

            log.info("로그인한 사용자: {}, 이메일: {}, 제공자: {}",
                    user.getNickname(), user.getEmail(), user.getProvider());

            // 1. 참여 중인 스터디 카운트 업데이트
            long studyCount = studyMemberRepository.countByUser(user);
            model.addAttribute("studyCount", studyCount);

            // 2. 최근 활동 목록 업데이트
            List<Notification> recentActivities = notificationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
            // Notification 엔티티에 iconClass 및 timeAgo 필드가 없으므로, DTO 변환 필요
            List<ActivityDto> activityDtos = recentActivities.stream()
                    .map(this::convertToActivityDto) // Notification을 ActivityDto로 변환하는 메서드
                    .collect(Collectors.toList());
            model.addAttribute("recentActivities", activityDtos);

            // 3. 내 현재 스터디 목록 (userStudies) 업데이트
//            // StudyMember에서 StudyGroup을 가져와야 함 (N+1 문제 방지 필요)
//            List<StudyMember> userStudyMembers = studyMemberRepository.findByUser(user);
//            List<StudyGroup> userStudies = userStudyMembers.stream()
//                    .map(StudyMember::getStudyGroup)
//                    .collect(Collectors.toList());

            // FETCH JOIN을 사용하여 N+1 문제 방지 및 필요한 데이터 한 번에 로드
            List<StudyMember> userStudyMembers = studyMemberRepository.findByUserWithStudyGroupAndLeader(user);
            List<StudyGroup> userStudies = userStudyMembers.stream()
                    .map(StudyMember::getStudyGroup)
                    .collect(Collectors.toList());

            model.addAttribute("userStudies", userStudies);


            // TODO: 다른 대시보드 데이터 로딩 (ScheduleCount, MessageCount, CompletedTasks 등)
            // 임시 데이터 (실제 값으로 변경 필요)
            model.addAttribute("scheduleCount", 0);
            model.addAttribute("messageCount", 0);
            model.addAttribute("completedTasks", 0);

        } else {
            // 로그인되지 않은 경우, 대시보드에 접근할 수 없도록 SecurityConfig에서 막아야 함
            // 또는 최소한의 정보를 표시하도록 처리
            model.addAttribute("user", null);
            model.addAttribute("studyCount", 0);
            model.addAttribute("recentActivities", List.of()); // 빈 리스트
            model.addAttribute("userStudies", List.of()); // 빈 리스트
            model.addAttribute("scheduleCount", 0);
            model.addAttribute("messageCount", 0);
            model.addAttribute("completedTasks", 0);
        }
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal CustomOAuth2User principal) { // 타입 캐스팅 필요 없음
        if (principal != null) { // 로그인된 사용자만 접근 가능하므로 principal은 null이 아닐 것
            User user = principal.getUser();
            model.addAttribute("user", user);
        }
        return "profile";
    }

    // Notification 엔티티를 Thymeleaf에서 사용하기 위한 ActivityDto로 변환하는 헬퍼 메서드
    private ActivityDto convertToActivityDto(Notification notification) {
        String iconClass;
        String description;
        String timeAgo; // 시간 표현 로직 추가 필요

        switch (notification.getType()) {
            case NEW_STUDY_CREATED:
                iconClass = "fas fa-users-class"; // 새로운 아이콘 클래스
                description = "새 스터디 그룹 <strong>\"" + notification.getMessage() + "\"</strong>이(가) 생성되었습니다.";
                break;
            case NEW_MESSAGE:
                iconClass = "fas fa-comment";
                description = "<strong>새로운 메시지:</strong> " + notification.getMessage();
                break;
            case SCHEDULE_UPDATED:
                iconClass = "fas fa-calendar-check";
                description = "<strong>일정 업데이트:</strong> " + notification.getMessage();
                break;
            case NEW_MEMBER_JOINED:
                iconClass = "fas fa-user-plus";
                description = "<strong>새 멤버:</strong> " + notification.getMessage();
                break;
            // 기타 알림 타입에 대한 처리 추가
            default:
                iconClass = "fas fa-info-circle";
                description = notification.getMessage();
                break;
        }

        // 시간 계산 로직 (간단한 예시, 더 정교하게 구현 가능)
        timeAgo = calculateTimeAgo(notification.getCreatedAt());

        return new ActivityDto(iconClass, description, timeAgo, notification.getNotificationId());
    }

    // 시간 계산을 위한 헬퍼 메서드 (간단한 예시)
    private String calculateTimeAgo(java.time.LocalDateTime dateTime) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(dateTime, now);

        if (duration.toMinutes() < 1) {
            return "방금 전";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + "분 전";
        } else if (duration.toDays() < 1) {
            return duration.toHours() + "시간 전";
        } else if (duration.toDays() < 7) {
            return duration.toDays() + "일 전";
        } else if (duration.toDays() < 30) {
            return (duration.toDays() / 7) + "주 전";
        } else if (duration.toDays() < 365) {
            return (duration.toDays() / 30) + "개월 전";
        } else {
            return (duration.toDays() / 365) + "년 전";
        }
    }
}
