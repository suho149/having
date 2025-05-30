package demo.having.domain.study.repository;

import demo.having.domain.study.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    // Fetch Join을 사용하여 StudyGroup과 관련된 StudyGroupTag, Tag를 한 번에 로드합니다.
    @Query("SELECT sg FROM StudyGroup sg JOIN FETCH sg.studyGroupTags sgt JOIN FETCH sgt.tag WHERE sg.studyGroupId = :studyGroupId")
    Optional<StudyGroup> findByIdWithTags(@Param("studyGroupId") Long studyGroupId);

    // 검색 관련 메소드
    // 1. 이름 또는 설명에 키워드가 포함된 스터디 검색 (페이징 포함)
    @Query("SELECT DISTINCT sg FROM StudyGroup sg WHERE sg.name LIKE %:keyword% OR sg.description LIKE %:keyword%")
    Page<StudyGroup> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 2. 특정 태그 이름을 포함하는 스터디 검색 (페이징 포함)
    @Query("SELECT DISTINCT sg FROM StudyGroup sg JOIN sg.studyGroupTags sgt JOIN sgt.tag t WHERE t.name = :tagName")
    Page<StudyGroup> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    // 3. 키워드와 태그를 모두 만족하는 스터디 검색 (페이징 포함)
    @Query("SELECT DISTINCT sg FROM StudyGroup sg JOIN sg.studyGroupTags sgt JOIN sgt.tag t " +
            "WHERE (sg.name LIKE %:keyword% OR sg.description LIKE %:keyword%) AND t.name IN :tagNames")
    Page<StudyGroup> findByKeywordAndTagNames(@Param("keyword") String keyword,
                                              @Param("tagNames") List<String> tagNames,
                                              Pageable pageable);

    // 4. 여러 태그 중 하나라도 포함하는 스터디 검색 (페이징 포함)
    @Query("SELECT DISTINCT sg FROM StudyGroup sg JOIN sg.studyGroupTags sgt JOIN sgt.tag t WHERE t.name IN :tagNames")
    Page<StudyGroup> findByTagNames(@Param("tagNames") List<String> tagNames, Pageable pageable);

    // 모든 스터디 그룹을 페이징하여 조회
    Page<StudyGroup> findAll(Pageable pageable);
}
