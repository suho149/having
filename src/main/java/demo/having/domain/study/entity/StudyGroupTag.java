package demo.having.domain.study.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder // 빌더 패턴 사용을 위해 추가
@AllArgsConstructor // 빌더 사용 시 모든 필드를 포함하는 생성자 필요
@Table(name = "study_group_tag") // 테이블 이름 명시 (선택 사항이지만 명확하게)
@EqualsAndHashCode(of = {"studyGroup", "tag"}) // 복합 키 대신, 두 엔티티로 동등성 비교
public class StudyGroupTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyGroupTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false) // 외래키 컬럼명 명시
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false) // 외래키 컬럼명 명시
    private Tag tag;

    // 이 setter는 StudyGroup 엔티티의 헬퍼 메서드에서만 호출되도록 보호합니다.
    protected void setStudyGroup(StudyGroup studyGroup) {
        this.studyGroup = studyGroup;
    }

    // --- 추가된 부분: Tag 필드에 대한 protected setter ---
    // 이 setter는 Tag 엔티티의 헬퍼 메서드에서만 호출되도록 보호합니다.
    protected void setTag(Tag tag) {
        this.tag = tag;
    }
}
