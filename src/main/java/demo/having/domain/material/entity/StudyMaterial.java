package demo.having.domain.material.entity;

import demo.having.domain.baseentity.BaseTimeEntity;
import demo.having.domain.study.entity.StudyGroup;
import demo.having.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class StudyMaterial extends BaseTimeEntity { // BaseTimeEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long materialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    // Post와의 관계는 필요성에 따라 결정 (아래 예시는 제거)
    // @ManyToOne(fetch = FetchType.LAZY)
    // private Post post;

    @Enumerated(EnumType.STRING)
    private MaterialType materialType; // Enum 사용

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private String fileName; // materialType이 FILE일 경우
    private String filePath; // materialType이 FILE일 경우
    private Long fileSize;   // materialType이 FILE일 경우
    private String linkUrl;  // materialType이 LINK일 경우

    // Builder는 필요한 필드만 포함하도록 조정
    @Builder
    public StudyMaterial(StudyGroup studyGroup, User uploader, MaterialType materialType, String title, String description, String fileName, String filePath, Long fileSize, String linkUrl) {
        this.studyGroup = studyGroup;
        this.uploader = uploader;
        this.materialType = materialType;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.linkUrl = linkUrl;
    }
}
