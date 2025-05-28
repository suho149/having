package demo.having.domain.study.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class StudyGroupTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyGroupTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tag tag;
}
