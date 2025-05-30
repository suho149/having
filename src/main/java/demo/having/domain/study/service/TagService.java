package demo.having.domain.study.service;

import demo.having.domain.study.entity.Tag;
import demo.having.domain.study.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    // @Transactional 어노테이션 제거
    public Tag findOrCreateNew(String tagName) {
        // Tag를 찾거나 새로운 Tag 엔티티 객체를 생성해서 반환만 합니다.
        // 여기서는 save를 호출하지 않습니다.
        return tagRepository.findByName(tagName)
                .orElseGet(() -> Tag.builder().name(tagName).build());
    }

    // 새로운 Tag를 명시적으로 저장하는 메서드를 추가할 수 있습니다.
    // 하지만 이 예시에서는 StudyGroupService에서 저장할 것입니다.
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }
}
