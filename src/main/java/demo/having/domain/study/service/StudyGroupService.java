package demo.having.domain.study.service;

import demo.having.domain.notification.service.NotificationService;
import demo.having.domain.study.dto.request.StudyGroupCreateDto;
import demo.having.domain.study.entity.*;
import demo.having.domain.study.repository.StudyGroupRepository;
import demo.having.domain.study.repository.StudyGroupTagRepository;
import demo.having.domain.study.repository.StudyMemberRepository;
import demo.having.domain.study.repository.TagRepository;
import demo.having.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final NotificationService notificationService;
    private final StudyGroupTagRepository studyGroupTagRepository;
    private final TagService tagService;
    private final TagRepository tagRepository;

    @Transactional
    public StudyGroup createNewStudyGroup(StudyGroupCreateDto studyGroupDto, User leader) {
        // 1. StudyGroup 엔티티 생성
        StudyGroup newStudyGroup = StudyGroup.builder()
                .leader(leader)
                .name(studyGroupDto.getName())
                .description(studyGroupDto.getDescription())
                .maxMembers(studyGroupDto.getMaxMembers())
                .locationType(studyGroupDto.getLocationType())
                .locationDetail(studyGroupDto.getLocationDetail())
                .startDate(studyGroupDto.getStartDate())
                .endDate(studyGroupDto.getEndDate())
                .status(StudyGroupStatus.RECRUITING)
                .build();

        StudyGroup savedStudyGroup = studyGroupRepository.save(newStudyGroup);

        // 2. 스터디를 생성한 리더를 StudyMember로 추가
        StudyMember leaderMember = StudyMember.builder()
                .studyGroup(savedStudyGroup)
                .user(leader)
                .isLeader(true)
                .build();
        studyMemberRepository.save(leaderMember);

        savedStudyGroup.addMember();

        // --- 태그 처리 로직 수정 시작 ---
        if (studyGroupDto.getTags() != null && !studyGroupDto.getTags().isEmpty()) {

            System.out.println("태그는 존재함");
            for (String tagName : studyGroupDto.getTags()) {
                String trimmedTagName = tagName.trim();

                System.out.println(trimmedTagName);

                // 1. Tag 엔티티를 데이터베이스에서 찾거나 새로운 객체 생성
                // TagService.findOrCreateNew()는 이제 DB 저장을 하지 않습니다.
                Optional<Tag> existingTag = tagRepository.findByName(trimmedTagName);
                Tag tag;

                if (existingTag.isPresent()) {
                    tag = existingTag.get();
                } else {
                    // 새 Tag 객체 생성 및 저장
                    tag = Tag.builder().name(trimmedTagName).build();
                    tagRepository.save(tag); // 여기서 새로운 Tag를 명시적으로 저장합니다.
                }

                // 2. StudyGroupTag 엔티티 생성 및 양방향 관계 설정
                StudyGroupTag studyGroupTag = StudyGroupTag.builder()
                        .studyGroup(savedStudyGroup)
                        .tag(tag)
                        .build();

                // StudyGroup 엔티티의 컬렉션에 추가 (StudyGroupTag의 setStudyGroup 호출)
                savedStudyGroup.addStudyGroupTag(studyGroupTag);
                // Tag 엔티티의 컬렉션에도 추가 (StudyGroupTag의 setTag 호출)
                tag.addStudyGroupTag(studyGroupTag);

                // StudyGroupTag는 StudyGroup의 cascade=ALL 설정에 의해 함께 저장됩니다.
                // 따라서 studyGroupTagRepository.save(studyGroupTag);는 필요하지 않습니다.
                // (만약 Tag의 cascade=ALL 설정도 사용한다면 마찬가지입니다.)
            }
        }
        // --- 태그 처리 로직 수정 끝 ---

//        // 태그 처리 로직 수정
//        if (studyGroupDto.getTags() != null && !studyGroupDto.getTags().isEmpty()) {
//            for (String tagName : studyGroupDto.getTags()) {
//                String trimmedTagName = tagName.trim();
//
//                if (trimmedTagName.isEmpty()) continue; // 빈 태그명 스킵
//
//                // 1. Tag 찾거나 생성
//                Optional<Tag> existingTag = tagRepository.findByName(trimmedTagName);
//                Tag tag;
//
//                if (existingTag.isPresent()) {
//                    tag = existingTag.get();
//                } else {
//                    tag = Tag.builder().name(trimmedTagName).build();
//                    tag = tagRepository.save(tag); // 새로운 Tag 저장
//                }
//
//                // 2. StudyGroupTag 생성 및 저장
//                StudyGroupTag studyGroupTag = StudyGroupTag.builder()
//                        .studyGroup(savedStudyGroup)
//                        .tag(tag)
//                        .build();
//
//                // 명시적으로 StudyGroupTag 저장
//                studyGroupTagRepository.save(studyGroupTag);
//
//                // 양방향 관계 설정
//                savedStudyGroup.addStudyGroupTag(studyGroupTag);
//                tag.addStudyGroupTag(studyGroupTag);
//            }
//        }

        // 3. 스터디 생성 알림 발행
        notificationService.createStudyGroupCreationNotification(leader, savedStudyGroup);

//        return savedStudyGroup;
        return studyGroupRepository.save(savedStudyGroup);
    }

    // 특정 ID의 스터디 그룹 조회
    @Transactional
    public StudyGroup getStudyGroupById(Long studyGroupId) {
//        return studyGroupRepository.findById(studyGroupId)
//                .orElseThrow(() -> new NoSuchElementException("ID " + studyGroupId + "를 가진 스터디 그룹을 찾을 수 없습니다."));
        return studyGroupRepository.findByIdWithTags(studyGroupId)
                .orElseThrow(() -> new NoSuchElementException("ID " + studyGroupId + "를 가진 스터디 그룹을 찾을 수 없습니다."));
    }

    // 사용자가 특정 스터디 그룹의 멤버인지 확인
    public boolean isUserStudyMember(StudyGroup studyGroup, User user) {
        if (user == null || studyGroup == null) {
            return false;
        }
        return studyMemberRepository.existsByStudyGroupAndUser(studyGroup, user);
    }

    // 사용자가 특정 스터디 그룹의 리더인지 확인
    public boolean isUserStudyLeader(StudyGroup studyGroup, User user) {
        if (user == null || studyGroup == null || studyGroup.getLeader() == null) {
            return false;
        }
        return studyGroup.getLeader().getUserId().equals(user.getUserId());
    }

    // 스터디 멤버 가입 (기능 추가 시 구현)
    @Transactional
    public void joinStudyGroup(StudyGroup studyGroup, User user) {
        // 이미 멤버인 경우 처리 (중복 가입 방지)
        if (isUserStudyMember(studyGroup, user)) {
            throw new IllegalStateException("이미 스터디 그룹의 멤버입니다.");
        }
        // 최대 멤버 수를 초과하는지 확인
        if (studyGroup.getCurrentMembersCount() >= studyGroup.getMaxMembers()) {
            throw new IllegalStateException("스터디 그룹의 최대 멤버 수를 초과했습니다.");
        }

        StudyMember newMember = StudyMember.builder()
                .studyGroup(studyGroup)
                .user(user)
                .isLeader(false) // 가입하는 멤버는 리더가 아님
                .build();
        studyMemberRepository.save(newMember);
        studyGroup.addMember(); // currentMembersCount 증가
    }

    // 스터디 멤버 탈퇴 (기능 추가 시 구현)
    @Transactional
    public void leaveStudyGroup(StudyGroup studyGroup, User user) {
        // 멤버가 아닌 경우 처리
        if (!isUserStudyMember(studyGroup, user)) {
            throw new IllegalStateException("이 스터디 그룹의 멤버가 아닙니다.");
        }
        // 리더는 탈퇴할 수 없음 (스터디 삭제 또는 리더 위임 기능 필요)
        if (isUserStudyLeader(studyGroup, user)) {
            throw new IllegalStateException("리더는 스터디를 탈퇴할 수 없습니다. 스터디를 삭제하거나 리더를 위임해주세요.");
        }

        studyMemberRepository.findByStudyGroupAndUser(studyGroup, user)
                .ifPresent(member -> {
                    studyMemberRepository.delete(member);
                    studyGroup.removeMember(); // currentMembersCount 감소
                });
    }

    // 사용자가 참여한 모든 스터디 그룹 목록을 가져오는 메서드
    @Transactional // Fetch Join으로 가져오므로 Transactional 유지
    public List<StudyGroup> getMyStudyGroups(User user) {
        return studyMemberRepository.findByUserWithStudyGroupAndLeader(user).stream()
                        .map(StudyMember::getStudyGroup).collect(Collectors.toList());
    }

    // 다른 스터디 그룹 관련 비즈니스 로직 (조회, 수정, 삭제 등)은 여기에 추가
}
