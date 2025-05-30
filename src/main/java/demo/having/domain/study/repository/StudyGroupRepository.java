package demo.having.domain.study.repository;

import demo.having.domain.study.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    // Fetch Join을 사용하여 StudyGroup과 관련된 StudyGroupTag, Tag를 한 번에 로드합니다.
    @Query("SELECT sg FROM StudyGroup sg JOIN FETCH sg.studyGroupTags sgt JOIN FETCH sgt.tag WHERE sg.studyGroupId = :studyGroupId")
    Optional<StudyGroup> findByIdWithTags(@Param("studyGroupId") Long studyGroupId);
}
