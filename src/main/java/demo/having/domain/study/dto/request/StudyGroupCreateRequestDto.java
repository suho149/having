package demo.having.domain.study.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class StudyGroupCreateRequestDto {

    @NotBlank(message = "스터디 그룹 이름은 필수입니다.")
    @Size(max = 100, message = "스터디 그룹 이름은 100자 이내여야 합니다.")
    private String name;

    @NotBlank(message = "설명은 필수입니다.")
    @Size(max = 2000, message = "설명은 2000자 이내여야 합니다.")
    private String description;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category; // 예: "프로그래밍", "어학", "취업준비" 등

    @NotNull(message = "최대 멤버 수는 필수입니다.")
    @Min(value = 2, message = "최대 멤버 수는 최소 2명 이상이어야 합니다.")
    private Integer maxMembers;

    @NotBlank(message = "모임 방식은 필수입니다.")
    private String locationType; // "ONLINE" 또는 "OFFLINE"

    private String locationDetail; // 온라인이면 URL, 오프라인이면 장소 설명

    @NotNull(message = "시작일은 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "시작일은 오늘이거나 미래여야 합니다.")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // 선택 사항, 미지정 시 무기한

    private String tags; // 쉼표로 구분된 태그 문자열 (예: "Java,Spring,JPA")

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getLocationDetail() {
        return locationDetail;
    }

    public void setLocationDetail(String locationDetail) {
        this.locationDetail = locationDetail;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
