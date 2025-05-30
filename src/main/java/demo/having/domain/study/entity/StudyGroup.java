package demo.having.domain.study.entity;

import demo.having.domain.baseentity.BaseTimeEntity;
import demo.having.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "studyGroupId")
public class StudyGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_group_id")
    private Long studyGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @Column(nullable = false, unique = true)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private int maxMembers;

    private int currentMembersCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyGroupStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType locationType;

    private String locationDetail;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    // --- 변경된 부분: ManyToMany -> OneToMany with StudyGroupTag ---
    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudyGroupTag> studyGroupTags = new HashSet<>();

    @Builder
    public StudyGroup(User leader, String name, String description,
                      int maxMembers, StudyGroupStatus status, LocationType locationType,
                      String locationDetail, LocalDate startDate, LocalDate endDate) { // tags 매개변수 제거
        this.leader = leader;
        this.name = name;
        this.description = description;
        this.maxMembers = maxMembers;
        this.currentMembersCount = 0;
        this.status = status;
        this.locationType = locationType;
        this.locationDetail = locationDetail;
        this.startDate = startDate;
        this.endDate = endDate;
        // this.studyGroupTags = new HashSet<>(); // Builder에서 직접 초기화할 필요 없음 (필드에서 이미 초기화)
    }

    public void addMember() {
        this.currentMembersCount++;
    }

    public void removeMember() {
        if (this.currentMembersCount > 0) {
            this.currentMembersCount--;
        }
    }

    // StudyGroupTag 엔티티를 통한 태그 관리 헬퍼 메서드
    public void addStudyGroupTag(StudyGroupTag studyGroupTag) {
        this.studyGroupTags.add(studyGroupTag);
        studyGroupTag.setStudyGroup(this); // 양방향 관계 설정
    }

    public void removeStudyGroupTag(StudyGroupTag studyGroupTag) {
        this.studyGroupTags.remove(studyGroupTag);
        studyGroupTag.setStudyGroup(null); // 양방향 관계 해제
    }

    // 스터디 그룹에 연결된 Tag 엔티티들을 직접 가져오는 편의 메서드 (Thymeleaf에서 사용)
    public Set<Tag> getTags() {
        return this.studyGroupTags.stream()
                .map(StudyGroupTag::getTag)
                .collect(Collectors.toSet());
    }

    // StudyGroup 정보 업데이트 메서드 (필요하다면)
    public void updateStudyGroup(String name, String description, int maxMembers,
                                 LocationType locationType, String locationDetail,
                                 LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.description = description;
        this.maxMembers = maxMembers;
        this.locationType = locationType;
        this.locationDetail = locationDetail;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // 상태 변경 메서드 (필요하다면)
    public void changeStatus(StudyGroupStatus status) {
        this.status = status;
    }
}