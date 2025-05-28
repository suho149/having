package demo.having.domain.post.entity;

import demo.having.domain.user.entity.User;
import demo.having.domain.study.entity.StudyGroup;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String postType;
    private String title;

    @Lob
    private String content;

    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
