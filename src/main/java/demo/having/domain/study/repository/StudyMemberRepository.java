package demo.having.domain.study.repository;

import demo.having.domain.study.entity.ApprovalStatus;
import demo.having.domain.study.entity.StudyGroup;
import demo.having.domain.study.entity.StudyMember;
import demo.having.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    Optional<StudyMember> findByStudyGroupAndUser(StudyGroup studyGroup, User user);

    boolean existsByStudyGroupAndUser(StudyGroup studyGroup, User user);

    long countByStudyGroup(StudyGroup studyGroup);

    // 특정 사용자가 참여하고 있는 스터디 멤버 수를 가져오는 메서드
    long countByUser(User user); // 새로 추가

    // 특정 사용자가 참여하고 있는 모든 스터디 그룹 목록을 가져오는 메서드 (대시보드 "내 현재 스터디"에 사용)
    List<StudyMember> findByUser(User user); // 새로 추가

    // 사용자가 참여한 스터디 그룹과 해당 스터디 그룹의 리더를 한 번의 쿼리로 가져오도록 FETCH JOIN 사용
    @Query("SELECT sm FROM StudyMember sm JOIN FETCH sm.studyGroup sg JOIN FETCH sg.leader WHERE sm.user = :user ORDER BY sm.joinDate DESC")
    List<StudyMember> findByUserWithStudyGroupAndLeader(@Param("user") User user); // 메서드명 변경 및 @Param 추가

    // --- 사용자의 승인된 스터디 멤버 목록을 스터디 그룹과 함께 가져오는 메소드 추가 ---
    @Query("SELECT sm FROM StudyMember sm JOIN FETCH sm.studyGroup WHERE sm.user = :user AND sm.approvalStatus = :approvalStatus ORDER BY sm.joinDate DESC")
    List<StudyMember> findByUserAndApprovalStatusWithStudyGroup(@Param("user") User user, @Param("approvalStatus") ApprovalStatus approvalStatus);
}
