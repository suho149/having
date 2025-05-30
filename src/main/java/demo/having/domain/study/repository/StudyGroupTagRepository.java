package demo.having.domain.study.repository;

import demo.having.domain.study.entity.StudyGroup;
import demo.having.domain.study.entity.StudyGroupTag;
import demo.having.domain.study.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyGroupTagRepository extends JpaRepository<StudyGroupTag, Long> {

    // 특정 StudyGroup과 Tag 조합으로 StudyGroupTag를 찾을 때 사용 (중복 방지 등)
    Optional<StudyGroupTag> findByStudyGroupAndTag(StudyGroup studyGroup, Tag tag);

    boolean existsByStudyGroupAndTag(StudyGroup studyGroup, Tag tag);
}
