package demo.having.domain.study.controller;

import demo.having.domain.study.dto.request.StudyGroupCreateDto;
import demo.having.domain.study.dto.request.StudyGroupUpdateDto;
import demo.having.domain.study.entity.LocationType;
import demo.having.domain.study.entity.StudyGroup;
import demo.having.domain.study.entity.StudyGroupStatus;
import demo.having.domain.study.service.StudyGroupService;
import demo.having.domain.user.entity.CustomOAuth2User;
import demo.having.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/study-groups")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    // 새 스터디 그룹 생성 폼 페이지
    @GetMapping("/new")
    public String newStudyGroupForm(Model model) {
        model.addAttribute("studyGroupCreateDto", new StudyGroupCreateDto());
        model.addAttribute("locationTypes", LocationType.values()); // Enum 값들을 뷰로 전달
        // Thymeleaf 폼에서 사용할 경우, @AuthenticationPrincipal을 통해 User 객체를 가져와서
        // 현재 로그인한 사용자 정보를 모델에 추가해줄 수도 있습니다 (선택 사항).
        // model.addAttribute("currentUser", currentUser);
        return "studyGroup/newStudyGroup"; // templates/studyGroup/newStudyGroup.html
    }

    // 새 스터디 그룹 생성 처리
    @PostMapping("/new")
    public String createNewStudyGroup(@Valid @ModelAttribute StudyGroupCreateDto studyGroupCreateDto,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal CustomOAuth2User principal, // 현재 로그인한 사용자 정보 (스프링 시큐리티 사용 가정)
                                      RedirectAttributes redirectAttributes,
                                      Model model) {

        System.out.println(studyGroupCreateDto.getName());
        System.out.println(studyGroupCreateDto.getDescription());
        System.out.println(studyGroupCreateDto.getTags());

        // 폼 유효성 검사
        if (bindingResult.hasErrors()) {
            model.addAttribute("locationTypes", LocationType.values()); // 에러 발생 시에도 Enum 값 다시 전달
            return "studyGroup/newStudyGroup";
        }

        // 로그인한 사용자 정보가 없는 경우 (예: 로그인 없이 접근 시)
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        try {
            StudyGroup newStudyGroup = studyGroupService.createNewStudyGroup(studyGroupCreateDto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "스터디 그룹 '" + newStudyGroup.getName() + "'이 성공적으로 생성되었습니다!");
            System.out.println("hello???" + newStudyGroup.getStudyGroupId());
            return "redirect:/study-groups/" + newStudyGroup.getStudyGroupId(); // 생성된 스터디 상세 페이지로 리다이렉트
        } catch (Exception e) {
            // 예외 처리 (예: 데이터베이스 오류 등)
            redirectAttributes.addFlashAttribute("errorMessage", "스터디 그룹 생성 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("locationTypes", LocationType.values()); // 에러 발생 시에도 Enum 값 다시 전달
            return "studyGroup/newStudyGroup"; // 다시 폼 페이지로
        }
    }

    // 스터디 편집 폼 페이지 (리더만 접근 가능)
    @GetMapping("/{studyGroupId}/manage") // 경로를 /manage로 수정했습니다.
    public String editStudyGroupForm(@PathVariable Long studyGroupId,
                                     Model model,
                                     @AuthenticationPrincipal CustomOAuth2User principal,
                                     RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        User currentUser = principal.getUser();

        try {
            StudyGroup studyGroup = studyGroupService.getStudyGroupById(studyGroupId);

            // 현재 사용자가 스터디 리더인지 확인
            if (!studyGroupService.isUserStudyLeader(studyGroup, currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", "스터디를 관리할 권한이 없습니다.");
                return "redirect:/study-groups/" + studyGroupId;
            }

            // StudyGroup 엔티티의 현재 정보를 StudyGroupUpdateDto로 변환하여 폼에 미리 채워줍니다.
            StudyGroupUpdateDto updateDto = new StudyGroupUpdateDto();
            updateDto.setStudyGroupId(studyGroup.getStudyGroupId());
            updateDto.setName(studyGroup.getName());
            updateDto.setDescription(studyGroup.getDescription());
            // 현재 태그들을 문자열 리스트로 변환하여 DTO에 설정 (Thymeleaf 폼 처리를 위해)
            List<String> currentTags = studyGroup.getStudyGroupTags().stream()
                    .map(sgt -> sgt.getTag().getName())
                    .collect(Collectors.toList());
            updateDto.setTags(currentTags);
            updateDto.setMaxMembers(studyGroup.getMaxMembers());
            updateDto.setLocationType(studyGroup.getLocationType());
            updateDto.setLocationDetail(studyGroup.getLocationDetail());
            updateDto.setStartDate(studyGroup.getStartDate());
            updateDto.setEndDate(studyGroup.getEndDate());
            updateDto.setStatus(studyGroup.getStatus());

            model.addAttribute("studyGroupUpdateDto", updateDto);
            model.addAttribute("locationTypes", LocationType.values());
            model.addAttribute("studyGroupStatuses", StudyGroupStatus.values()); // 스터디 상태 Enum 전달
            model.addAttribute("studyGroup", studyGroup); // 스터디 상세 정보 (리더 여부 확인 등)
            return "studyGroup/studyGroupEdit"; // templates/studyGroup/studyGroupEdit.html
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디를 찾을 수 없습니다: " + e.getMessage());
            return "redirect:/study-groups/my-studies";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디 편집 페이지 로딩 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/study-groups/my-studies";
        }
    }

    // 스터디 편집 처리 (리더만 가능)
    @PostMapping("/{studyGroupId}/manage") // 경로를 /manage로 수정했습니다.
    public String updateStudyGroup(@PathVariable Long studyGroupId,
                                   @Valid @ModelAttribute StudyGroupUpdateDto studyGroupUpdateDto,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal CustomOAuth2User principal,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        User currentUser = principal.getUser();

        // PathVariable의 studyGroupId와 DTO의 studyGroupId가 일치하는지 확인 (보안 강화)
        if (!studyGroupId.equals(studyGroupUpdateDto.getStudyGroupId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "잘못된 요청입니다.");
            return "redirect:/study-groups/my-studies";
        }

        // 폼 유효성 검사
        if (bindingResult.hasErrors()) {
            model.addAttribute("locationTypes", LocationType.values());
            model.addAttribute("studyGroupStatuses", StudyGroupStatus.values());
            // 기존 스터디 정보를 다시 불러와 모델에 추가 (폼 재렌더링 시 필요)
            try {
                StudyGroup studyGroup = studyGroupService.getStudyGroupById(studyGroupId);
                model.addAttribute("studyGroup", studyGroup);
            } catch (NoSuchElementException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "스터디를 찾을 수 없습니다.");
                return "redirect:/study-groups/my-studies";
            }
            return "studyGroup/studyGroupEdit";
        }

        try {
            StudyGroup updatedStudyGroup = studyGroupService.updateStudyGroup(studyGroupUpdateDto, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "스터디 그룹 '" + updatedStudyGroup.getName() + "'이 성공적으로 업데이트되었습니다!");
            return "redirect:/study-groups/" + updatedStudyGroup.getStudyGroupId(); // 업데이트된 스터디 상세 페이지로 리다이렉트
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/study-groups/my-studies";
        } catch (IllegalAccessException e) { // 리더 권한 없음 예외
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/study-groups/" + studyGroupId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디 그룹 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("locationTypes", LocationType.values());
            model.addAttribute("studyGroupStatuses", StudyGroupStatus.values());
            // 오류 발생 시 기존 스터디 정보 다시 로드
            try {
                StudyGroup studyGroup = studyGroupService.getStudyGroupById(studyGroupId);
                model.addAttribute("studyGroup", studyGroup);
            } catch (NoSuchElementException ex) {
                // 이 경우는 발생하기 어려움 (이미 위에서 찾았으므로)
            }
            return "studyGroup/studyGroupEdit";
        }
    }

    @GetMapping("/{studyGroupId}")
    public String getStudyGroupDetail(@PathVariable Long studyGroupId,
                                      Model model,
                                      @AuthenticationPrincipal CustomOAuth2User principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            StudyGroup studyGroup = studyGroupService.getStudyGroupById(studyGroupId);
            model.addAttribute("studyGroup", studyGroup);

            // 로그인된 사용자 정보가 있다면, 스터디 참여 여부 및 리더 여부 확인
            boolean isMember = false;
            boolean isLeader = false;
            if (principal != null && principal.getUser() != null) {
                User currentUser = principal.getUser();
                model.addAttribute("currentUser", currentUser); // Thymeleaf에서 사용자 정보 접근 위함

                isMember = studyGroupService.isUserStudyMember(studyGroup, currentUser);
                isLeader = studyGroupService.isUserStudyLeader(studyGroup, currentUser);
            }
            model.addAttribute("isMember", isMember);
            model.addAttribute("isLeader", isLeader);

            // 가입/탈퇴 버튼 표시를 위한 추가 데이터 (선택 사항)
            model.addAttribute("canJoin", !isMember && studyGroup.getCurrentMembersCount() < studyGroup.getMaxMembers());
            model.addAttribute("canLeave", isMember && !isLeader);


            return "studyGroup/studyGroupDetail"; // templates/studyGroup/studyGroupDetail.html
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/study-groups"; // 스터디 목록 페이지로 리다이렉트 (또는 404 페이지)
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디 그룹 상세 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/study-groups";
        }
    }

    @PostMapping("/{studyGroupId}/join")
    public String joinStudyGroup(@PathVariable Long studyGroupId,
                                 @AuthenticationPrincipal CustomOAuth2User principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        User currentUser = principal.getUser();

        try {
            StudyGroup studyGroup = studyGroupService.getStudyGroupById(studyGroupId);
            studyGroupService.joinStudyGroup(studyGroup, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "'" + studyGroup.getName() + "' 스터디에 가입 요청을 보냈습니다. 리더의 승인을 기다려주세요.");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디를 찾을 수 없습니다: " + e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디 가입 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/study-groups/" + studyGroupId; // 수정: studies -> study-groups
    }

    @PostMapping("/{studyGroupId}/leave")
    public String leaveStudyGroup(@PathVariable Long studyGroupId,
                                  @AuthenticationPrincipal CustomOAuth2User principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        User currentUser = principal.getUser();

        try {
            StudyGroup studyGroup = studyGroupService.getStudyGroupById(studyGroupId);
            studyGroupService.leaveStudyGroup(studyGroup, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "'" + studyGroup.getName() + "' 스터디에서 성공적으로 탈퇴했습니다.");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디를 찾을 수 없습니다: " + e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디 탈퇴 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/study-groups/" + studyGroupId; // 수정: studies -> study-groups
    }

    // 내 스터디 목록 페이지
    @GetMapping("/my-studies")
    public String getMyStudyGroups(Model model, @AuthenticationPrincipal CustomOAuth2User principal, RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }
        User currentUser = principal.getUser();
        // StudyGroupService를 통해 사용자가 참여한 모든 스터디 그룹 목록을 가져옴
        List<StudyGroup> myStudyGroups = studyGroupService.getMyStudyGroups(currentUser);
        model.addAttribute("myStudyGroups", myStudyGroups);
        model.addAttribute("currentUser", currentUser); // 템플릿에서 사용자 정보 활용을 위해
        return "studyGroup/myStudies"; // templates/studyGroup/myStudies.html
    }

    // 스터디 검색 및 전체 목록 조회
    @GetMapping
    public String searchStudyGroups(@RequestParam(name = "keyword", required = false) String keyword,
                                    @RequestParam(name = "tags", required = false) List<String> tags,
                                    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                    Model model) {

        Page<StudyGroup> studyGroupPage = studyGroupService.searchStudyGroups(keyword, tags, pageable);

        model.addAttribute("studyGroupPage", studyGroupPage);
        model.addAttribute("keyword", keyword); // 검색어 유지
        model.addAttribute("selectedTags", tags); // 선택된 태그 유지

        int totalPages = studyGroupPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "studyGroup/studyGroupList"; // 스터디 목록을 보여줄 Thymeleaf 템플릿
    }

}