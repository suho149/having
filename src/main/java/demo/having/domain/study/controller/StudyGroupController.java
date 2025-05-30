package demo.having.domain.study.controller;

import demo.having.domain.study.dto.request.StudyGroupCreateDto;
import demo.having.domain.study.entity.LocationType;
import demo.having.domain.study.entity.StudyGroup;
import demo.having.domain.study.service.StudyGroupService;
import demo.having.domain.user.entity.CustomOAuth2User;
import demo.having.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

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

    // --- 스터디 가입/탈퇴 기능 (추후 구현) ---
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
            redirectAttributes.addFlashAttribute("successMessage", "'" + studyGroup.getName() + "' 스터디에 성공적으로 가입했습니다!");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디를 찾을 수 없습니다: " + e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스터디 가입 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/studies/" + studyGroupId;
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
        return "redirect:/studies/" + studyGroupId;
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

}