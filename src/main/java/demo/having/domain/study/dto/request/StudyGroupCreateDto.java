package demo.having.domain.study.dto.request;

import demo.having.domain.study.entity.LocationType;
import demo.having.domain.study.entity.StudyGroupStatus;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class StudyGroupCreateDto {

    @NotBlank(message = "스터디 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "스터디 이름은 2자 이상 50자 이하여야 합니다.")
    private String name;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    private String description;

    private List<String> tags;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 100, message = "최대 100명까지 가능합니다.")
    private int maxMembers;

    @NotNull(message = "장소 유형을 선택해주세요.")
    private LocationType locationType;

    @Size(max = 100, message = "상세 장소는 100자 이하여야 합니다.")
    private String locationDetail; // 온라인 스터디의 경우 비워둘 수 있음

    @NotNull(message = "시작일은 필수입니다.")
    @FutureOrPresent(message = "시작일은 오늘 또는 미래 날짜여야 합니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // 선택 사항 (null 허용)

    // 새로운 스터디는 기본적으로 RECRUITING 상태로 시작
    private StudyGroupStatus status = StudyGroupStatus.RECRUITING;
}
