package demo.having.domain.material.entity;

import demo.having.domain.post.entity.Post;
import demo.having.domain.study.entity.StudyGroup;
import demo.having.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class StudyMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long materialId;

    @ManyToOne(fetch = FetchType.LAZY)
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    private User uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private String materialType;
    private String title;

    @Lob
    private String description;

    private String fileName;
    private String filePath;
    private Long fileSize;
    private String linkUrl;
    private LocalDateTime createdAt;
}
