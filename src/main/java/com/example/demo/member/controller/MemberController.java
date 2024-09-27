package com.example.demo.member.controller;

import com.example.demo.global.email.service.EmailService;
import com.example.demo.global.exception.PasswordMismatchException;
import com.example.demo.member.dto.EditForm;
import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // 로그인 페이지
    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String loginPage() {
        return "member/login";
    }

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupPage() {
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid SignForm signForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "member/signup";
        }
        try {
            memberService.signup(
                    signForm.getAddress(),
                    signForm.getUsername(),
                    signForm.getPassword(),
                    signForm.getPassword_confirm(),
                    signForm.getNickname(),
                    signForm.getEmail(),
                    0L,
                    signForm.getThumnailImg()
            );
        } catch (DataIntegrityViolationException | PasswordMismatchException e) {
            model.addAttribute("message", e.getMessage());
            model.addAttribute("searchUrl", "/member/signup");
            return "admin/Message";
        }
        model.addAttribute("message", "회원가입이 완료되었습니다!");
        model.addAttribute("searchUrl", "/member/login");
        return "admin/Message";
    }

    // ID 찾기
    @GetMapping("/findId")
    public String findIdPage() {
        return "member/findId";
    }

    @PostMapping("/findId")
    public String findId(@RequestParam("email") String email, Model model) {
        List<Member> members = memberService.findByUserEmail(email);
        if (members.isEmpty()) {
            model.addAttribute("message", "입력하신 이메일로 등록된 계정이 없습니다.");
            model.addAttribute("searchUrl", "/member/findId");
            return "admin/Message";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><h2>당신의 Winding 아이디는 다음과 같습니다:</h2><ul>");
        for (Member member : members) {
            sb.append("<li>").append(member.getUsername()).append("</li>");
        }
        sb.append("</ul></body></html>");

        emailService.sendHtml(email, "당신의 Winding 아이디 입니다!", sb.toString());

        model.addAttribute("message", "이메일이 정상적으로 발송되었습니다.");
        model.addAttribute("searchUrl", "/member/login");
        return "admin/Message";
    }

    // 비밀번호 찾기
    @GetMapping("/findPw")
    public String findPwPage() {
        return "member/findPw";
    }

    @PostMapping("/findPw")
    public String findPassword(@RequestParam("username") String username, Model model) {
        Optional<Member> optionalMember = memberService.findByUsername(username);
        if (optionalMember.isEmpty()) {
            model.addAttribute("message", "입력하신 아이디는 존재하지 않습니다.");
            model.addAttribute("searchUrl", "/member/findPw");
            return "admin/Message";
        }

        Member member = optionalMember.get();
        String temporaryPassword = generateTemporaryPassword();
        member.setPassword(passwordEncoder.encode(temporaryPassword));
        memberService.save(member);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><h2>임시 비밀번호가 발급되었습니다:</h2>");
        sb.append("<p>임시 비밀번호: ").append(temporaryPassword).append("</p>");
        sb.append("<p>로그인 후 반드시 비밀번호를 변경해 주세요.</p></body></html>");

        emailService.sendHtml(member.getEmail(), "임시 비밀번호 발급 안내", sb.toString());

        model.addAttribute("message", "이메일이 정상적으로 발송되었습니다.");
        model.addAttribute("searchUrl", "/member/login");
        return "admin/Message";
    }

    // 임시 비밀번호 생성 메소드
    private String generateTemporaryPassword() {
        int length = 8;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 회원 정보 수정 페이지
    @GetMapping("/edit")
    public String editMemberForm(Model model, Authentication authentication) {
        // 로그인된 사용자 이름 가져오기
        String username = authentication.getName();

        // 사용자 정보를 Optional로 처리
        Optional<Member> optionalMember = memberService.findByUsername(username);

        // 사용자가 존재하지 않을 경우 예외 처리
        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        // Optional에서 Member 객체 가져오기
        Member member = optionalMember.get();

        // 모델에 member 데이터 추가
        model.addAttribute("member", member);

        return "member/edit";  // edit.html 템플릿 반환
    }

    @PostMapping("/edit")
    public String editProfile(@Valid @ModelAttribute EditForm editForm, BindingResult bindingResult, Model model, Authentication authentication) {
        // 유효성 검사 실패 시 다시 회원 정보 수정 페이지로
        if (bindingResult.hasErrors()) {
            String username = authentication.getName();
            Optional<Member> optionalMember = memberService.findByUsername(username);

            if (optionalMember.isEmpty()) {
                throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
            }

            Member member = optionalMember.get();
            model.addAttribute("member", member);
            return "member/edit";
        }

        // 로그인된 사용자 이름 가져오기
        String username = authentication.getName();

        // 회원 정보 업데이트
        memberService.updateMember(username, editForm.getNickname(), editForm.getEmail(), editForm.getProfileImg());

        // 수정 완료 후 메인 페이지로 리디렉션
        return "redirect:/";
    }


    // 기본 프로필 설정
    @PostMapping("/setDefaultProfile")
    public String setDefaultProfile() {
        String username = getAuthenticatedUsername();
        memberService.setDefaultProfile(username);
        return "redirect:/";
    }

    // 회원 탈퇴
    @PostMapping("/delete")
    @ResponseBody
    public String deleteMember(HttpServletRequest request, HttpServletResponse response) {
        String username = getAuthenticatedUsername();
        memberService.deleteMember(username);
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "success";
    }

    // 비밀번호 변경 페이지
    @GetMapping("/editPw")
    public String editPasswordForm(Model model) {
        model.addAttribute("editPasswordForm", new EditPasswordForm());
        return "member/editPw";
    }

    @PostMapping("/editPw")
    public String editPasswordSubmit(@Valid @ModelAttribute EditPasswordForm editPasswordForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "member/editPw";
        }

        String username = getAuthenticatedUsername();
        boolean changePasswordSuccess = memberService.changePassword(username, editPasswordForm.getCurrentPassword(), editPasswordForm.getNewPassword());

        if (!changePasswordSuccess) {
            model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
            return "member/editPw";
        }

        return "redirect:/";
    }

    // 마이페이지
    @GetMapping("/mypage")
    public String myPage(Model model, Authentication authentication) {
        // 로그인된 사용자 이름 가져오기
        String username = authentication.getName();

        // 사용자 정보를 Optional로 처리
        Optional<Member> optionalMember = memberService.findByUsername(username);

        // 사용자가 존재하지 않을 경우 예외 처리
        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        // Optional에서 Member 객체 가져오기
        Member member = optionalMember.get();

        // 모델에 member 데이터를 추가
        model.addAttribute("member", member);
        model.addAttribute("wishlist", member.getWishlist()); // 찜 목록
        model.addAttribute("cart", member.getCart());         // 장바구니 목록
        model.addAttribute("mileage", member.getMileage());   // 마일리지

        return "member/mypage";  // mypage.html 템플릿으로 이동
    }


    // 인증된 사용자의 이름을 반환하는 유틸리티 메서드
    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @Data
    public static class SignForm {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotBlank
        private String password_confirm;
        @NotBlank
        private String nickname;
        @NotBlank
        private String email;
        @NotEmpty(message = "주소는 필수 입력 사항입니다.")
        private String address;
        private Long hit;
        private String thumnailImg;
    }

    @Data
    public static class EditPasswordForm {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;
    }
}