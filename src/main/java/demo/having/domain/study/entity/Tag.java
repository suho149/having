package demo.having.domain.study.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "name")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // --- 변경된 부분: ManyToMany -> OneToMany with StudyGroupTag ---
    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudyGroupTag> studyGroupTags = new HashSet<>();

    @Builder
    public Tag(String name) {
        this.name = name;
    }

    // 양방향 관계 편의 메서드 (선택 사항이지만, 관계의 일관성을 위해 고려)
    // StudyGroupTag 엔티티에서 양방향 관계를 관리하므로 여기서는 단순 getter/setter만 있어도 됨
    // 혹은 TagService에서 StudyGroupTag 생성 시 Tag의 studyGroupTags에 추가하는 로직 구현

    // 특정 스터디 그룹 태그 추가 (Tag 입장에서는 Many-to-One)
    public void addStudyGroupTag(StudyGroupTag studyGroupTag) {
        this.studyGroupTags.add(studyGroupTag);
        studyGroupTag.setTag(this); // 양방향 관계 설정
    }

    public void removeStudyGroupTag(StudyGroupTag studyGroupTag) {
        this.studyGroupTags.remove(studyGroupTag);
        studyGroupTag.setTag(null); // 양방향 관계 해제
    }
}
