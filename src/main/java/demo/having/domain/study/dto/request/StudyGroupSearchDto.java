package demo.having.domain.study.dto.request;

import demo.having.domain.study.entity.LocationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyGroupSearchDto {
    private String keyword; // 스터디 이름 또는 설명 검색
    private String tag;     // 태그 검색 (단일 태그)
    private LocationType locationType; // 진행 방식 검색
    private Boolean recruitingOnly = true; // 모집 중인 스터디만 보기 (기본값 true)
    private Integer minMembers; // 최소 멤버 수
    private Integer maxMembers; // 최대 멤버 수
    // 추가적인 검색 조건 (예: 기간 등) 필요시 추가
}
