package demo.having.domain.study.service;

import demo.having.domain.notification.service.NotificationService;
import demo.having.domain.study.dto.request.StudyGroupCreateDto;
import demo.having.domain.study.dto.request.StudyGroupUpdateDto;
import demo.having.domain.study.entity.*;
import demo.having.domain.study.repository.StudyGroupRepository;
import demo.having.domain.study.repository.StudyGroupTagRepository;
import demo.having.domain.study.repository.StudyMemberRepository;
import demo.having.domain.study.repository.TagRepository;
import demo.having.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
                .approvalStatus(ApprovalStatus.APPROVED)
                .joinDate(LocalDate.now().atStartOfDay()) // 가입일도 명시적으로 설정하는 것이 좋습니다.
                .roleInStudy(StudyRole.LEADER) // 리더의 역할도 명시적으로 지정
                .build();
        studyMemberRepository.save(leaderMember);

        savedStudyGroup.addMember(); // 이 시점에 멤버 카운트 증가 로직이 있다면 여기서 실행

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

        // 3. 스터디 생성 알림 발행
        notificationService.createStudyGroupCreationNotification(leader, savedStudyGroup);

//        return savedStudyGroup;
        return studyGroupRepository.save(savedStudyGroup);
    }

    @Transactional
    public StudyGroup updateStudyGroup(StudyGroupUpdateDto updateDto, User currentUser) throws IllegalAccessException {
        StudyGroup studyGroup = studyGroupRepository.findByIdWithTags(updateDto.getStudyGroupId())
                .orElseThrow(() -> new NoSuchElementException("ID " + updateDto.getStudyGroupId() + "를 가진 스터디 그룹을 찾을 수 없습니다."));

        // 현재 사용자가 스터디 리더인지 확인
        if (!isUserStudyLeader(studyGroup, currentUser)) {
            throw new IllegalAccessException("스터디 그룹을 수정할 권한이 없습니다.");
        }

        // DTO의 정보를 바탕으로 StudyGroup 엔티티 업데이트
        studyGroup.updateStudyGroup(
                updateDto.getName(),
                updateDto.getDescription(),
                updateDto.getMaxMembers(),
                updateDto.getLocationType(),
                updateDto.getLocationDetail(),
                updateDto.getStartDate(),
                updateDto.getEndDate(),
                updateDto.getStatus() // StudyGroup 엔티티의 updateStudyGroup 메서드에 status 추가했으므로 전달
        );

        // 태그 업데이트 로직
        // 1. 기존 태그들을 모두 제거
        studyGroupTagRepository.deleteAll(studyGroup.getStudyGroupTags());
        studyGroup.clearStudyGroupTags();

        // 2. 새로운 태그들을 추가
        if (updateDto.getTags() != null && !updateDto.getTags().isEmpty()) {
            for (String tagName : updateDto.getTags()) {
                String trimmedTagName = tagName.trim();
                if (trimmedTagName.isEmpty()) continue; // 빈 태그는 건너뜁니다.

                Optional<Tag> existingTag = tagRepository.findByName(trimmedTagName);
                Tag tag;

                if (existingTag.isPresent()) {
                    tag = existingTag.get();
                } else {
                    tag = Tag.builder().name(trimmedTagName).build();
                    tagRepository.save(tag); // 새로운 Tag는 여기서 저장
                }

                StudyGroupTag studyGroupTag = StudyGroupTag.builder()
                        .studyGroup(studyGroup)
                        .tag(tag)
                        .build();
                studyGroup.addStudyGroupTag(studyGroupTag); // 양방향 관계 설정 및 컬렉션에 추가
                tag.addStudyGroupTag(studyGroupTag);
            }
        }

        // 변경된 StudyGroup 엔티티 저장 (Transactional 어노테이션 덕분에 변경 감지 후 자동으로 저장될 수 있음)
        return studyGroupRepository.save(studyGroup);
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

    // 스터디 멤버 가입
    @Transactional
    public void joinStudyGroup(StudyGroup studyGroup, User user) {
        if (isUserStudyMember(studyGroup, user)) {
            throw new IllegalStateException("이미 스터디 그룹의 멤버입니다.");
        }
        if (studyGroup.getCurrentMembersCount() >= studyGroup.getMaxMembers()) {
            throw new IllegalStateException("스터디 그룹의 최대 멤버 수를 초과했습니다.");
        }

        StudyMember newMember = StudyMember.builder()
                .studyGroup(studyGroup)
                .user(user)
                .isLeader(false)
                .joinDate(LocalDate.now().atStartOfDay()) // 가입일 설정
                .approvalStatus(ApprovalStatus.PENDING) // 가입 시 초기 상태는 '대기'
                .roleInStudy(StudyRole.MEMBER) // 일반 멤버 역할 부여
                .build();
        studyMemberRepository.save(newMember);
        // studyGroup.addMember(); // 승인 대기 상태에서는 멤버 카운트를 증가시키지 않습니다.

        // 스터디 리더에게 가입 요청 알림 (선택 사항)
        notificationService.createStudyJoinRequestNotification(user, studyGroup.getLeader(), studyGroup);
    }

    // 스터디 멤버 탈퇴
    @Transactional
    public void leaveStudyGroup(StudyGroup studyGroup, User user) {
        if (!isUserStudyMember(studyGroup, user)) {
            throw new IllegalStateException("이 스터디 그룹의 멤버가 아닙니다.");
        }
        if (isUserStudyLeader(studyGroup, user)) {
            throw new IllegalStateException("리더는 스터디를 탈퇴할 수 없습니다. 스터디를 삭제하거나 리더를 위임해주세요.");
        }

        studyMemberRepository.findByStudyGroupAndUser(studyGroup, user)
                .ifPresent(member -> {
                    studyMemberRepository.delete(member);
                    // approvalStatus가 APPROVED인 경우에만 count 감소
                    if (member.getApprovalStatus() == ApprovalStatus.APPROVED) {
                        studyGroup.removeMember();
                    }
                });
    }

    // 사용자가 참여한 모든 스터디 그룹 목록을 가져오는 메서드
    @Transactional
    public List<StudyGroup> getMyStudyGroups(User user) {
        return studyMemberRepository.findByUserAndApprovalStatusWithStudyGroup(user, ApprovalStatus.APPROVED).stream()
                .map(StudyMember::getStudyGroup)
                .collect(Collectors.toList());
    }

    // --- 스터디 검색 메소드 추가 시작 ---
    @Transactional(readOnly = true)
    public Page<StudyGroup> searchStudyGroups(String keyword, List<String> tagNames, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty() && tagNames != null && !tagNames.isEmpty()) {
            // 키워드와 태그 모두 있을 경우
            return studyGroupRepository.findByKeywordAndTagNames(keyword.trim(), tagNames, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드만 있을 경우
            return studyGroupRepository.findByKeyword(keyword.trim(), pageable);
        } else if (tagNames != null && !tagNames.isEmpty()) {
            // 태그만 있을 경우
            return studyGroupRepository.findByTagNames(tagNames, pageable);
        } else {
            // 검색 조건이 없을 경우 (전체 스터디 페이징 조회)
            return studyGroupRepository.findAll(pageable);
        }
    }
    // --- 스터디 검색 메소드 추가 끝 ---

    // 다른 스터디 그룹 관련 비즈니스 로직 (조회, 수정, 삭제 등)은 여기에 추가
}
