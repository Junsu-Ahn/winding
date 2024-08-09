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

    @Autowired
    private AuthenticationManager authenticationManager;

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    @ControllerAdvice
    @RequiredArgsConstructor
    public class GlobalControllerAdvice {
        private final MemberRepository memberService;

        @ModelAttribute
        public void addAttributes(Model model) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String username = auth.getName();
                Member member = memberService.findByUsername(username).orElse(null);
                if (member != null) {
                    model.addAttribute("currentMember", member);
                }
            }
        }

    }


    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String loginPage() {

        return "member/login";
    }



    @PostMapping("/login")
    public String login() {
        return "member/login";

       /* @PostMapping("/login")
        public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
            Optional<Member> optionalMember = memberRepository.findByUsername(loginRequest.getUsername());
            Member member = optionalMember.get();
            // 사용자의 Role 정보를 가져옴 (여기서는 Enum으로 가정)
            String role = member.getRole().toString();

            // Role 정보를 Spring Security의 SecurityContext에 등록
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), null, AuthorityUtils.createAuthorityList(role));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            checkUserAuthorities(authentication);

            return ResponseEntity.ok("로그인 성공. 사용자의 Role: " + role);
        }*/
    }

    private void checkUserAuthorities(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // 사용자가 가진 모든 권한을 가져오기
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                System.out.println("사용자 권한: " + authority.getAuthority());
            }
            // 여기에 추가적인 작업을 수행할 수 있음
        }
    }


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
                    signForm.getAddress(), // address 필드 추가
                    signForm.getUsername(),
                    signForm.getPassword(),
                    signForm.getPassword_confirm(),
                    signForm.getNickname(),
                    signForm.getEmail(),
                    0L,
                    signForm.getThumnailImg()
            );
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("message", e.getMessage());
            model.addAttribute("searchUrl", "/member/signup");
            return "admin/Message";
        } catch (PasswordMismatchException e) {
            model.addAttribute("message", e.getMessage());
            model.addAttribute("searchUrl", "/member/signup");
            return "admin/Message";
        }
        model.addAttribute("message", "회원가입이 완료되었습니다!");
        model.addAttribute("searchUrl", "/member/login");
        return "admin/Message";
    }

    @GetMapping("/findId")
    public String find_id() {
        return "member/findId";
    }

    @PostMapping("/findId")
    public String find_id2(@RequestParam("email") String email, Model model) {
        List<Member> members = memberService.findByUserEmail(email);
        if(members.isEmpty())  // 멤버를 찾을 수 없는 경우 처리
        {
            model.addAttribute("message", "입력하신 이메일로 등록된 계정이 없습니다.");
            model.addAttribute("searchUrl", "/member/findId");
            return "admin/Message";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>당신의 오내요 아이디는 다음과 같습니다:</h2>");
        sb.append("<ul>");

        // 멤버들의 아이디를 StringBuilder에 추가
        for (Member member : members) {
            sb.append("<li>").append(member.getUsername()).append("</li>");
        }

        sb.append("</ul>");
        sb.append("</body></html>");

        // 이메일 발송
        emailService.sendHtml(email, "당신의 오내요 아이디 입니다!", sb.toString());

        model.addAttribute("message", "이메일이 정상적으로 발송되었습니다.");
        model.addAttribute("searchUrl","/member/login");

        return "admin/Message";
    }


    @GetMapping("/findPw")
    public String find_pw() {
        return "member/findPw";
    }

    @PostMapping("/findPw")
    public String find_password(@RequestParam("username") String username, Model model) {
        // 유저를 아이디로 조회
        Optional<Member> optionalMember = memberService.findByUsername(username);

        // 유저를 찾을 수 없는 경우 처리
        if (!optionalMember.isPresent()) {
            model.addAttribute("message", "입력하신 아이디는 존재하지 않습니다.");
            model.addAttribute("searchUrl", "/member/findPw");
            return "admin/Message";
        }

        Member member = optionalMember.get();

        // 임시 비밀번호 생성
        String temporaryPassword = generateTemporaryPassword();

        // 유저의 비밀번호를 임시 비밀번호로 변경
        member.setPassword(passwordEncoder.encode(temporaryPassword));
        memberService.save(member); // 비밀번호 변경 사항을 저장

        // 이메일 내용 작성
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>임시 비밀번호가 발급되었습니다:</h2>");
        sb.append("<p>임시 비밀번호: ").append(temporaryPassword).append("</p>");
        sb.append("<p>로그인 후 반드시 비밀번호를 변경해 주세요.</p>");
        sb.append("</body></html>");

        // 이메일 발송
        emailService.sendHtml(member.getEmail(), "임시 비밀번호 발급 안내", sb.toString());

        model.addAttribute("message", "이메일이 정상적으로 발송되었습니다.");
        model.addAttribute("searchUrl","/member/login");

        return "admin/Message";
    }

    // 임시 비밀번호 생성 메소드
    private String generateTemporaryPassword() {
        // 임시 비밀번호 생성 로직 (여기서는 8자리의 랜덤 문자열을 생성)
        int length = 8;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /*@GetMapping("/admin/memberList")
    @PreAuthorize("hasRole('ADMIN')")
    public String memberList(Model model) {
        List<Member> members = memberService.getAllMembers();
        for (Member member : members) {
            Long totalHits = memberService.calculateTotalHitsForMember(member.getId());
            member.setHit(totalHits); // Member 엔티티의 hit 필드에 총 조회수 설정
        }
        model.addAttribute("members", members);
        return "admin/memberList";
    }*/


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/deleteMember/{username}")  // GetMapping 사용 가능
    public String deleteMemberByAdmin(@PathVariable("username") String username) {
        try {
            Member member = memberService.getMemberByUsername(username);
            memberService.delete(member);
            return "redirect:/member/admin/memberList";  // 회원 목록 페이지로 리다이렉트
        } catch (IllegalArgumentException e) {
            // 삭제 실패 시 에러 메시지를 회원 목록 페이지로 전달
            return "redirect:/admin/members?error=true";
        }


    }
    // 추가

    @GetMapping("/edit")
    public String editMemberForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Member member = memberService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        model.addAttribute("member", member);
        return "member/edit";
    }

    @PostMapping("/edit")
    public String editProfile(@Valid @ModelAttribute EditForm editForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Member member = memberService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
            model.addAttribute("member", member);
            return "member/edit";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        memberService.updateMember(username, editForm.getNickname(), editForm.getEmail(), editForm.getProfileImg());

        return "redirect:/"; // 메인 화면으로 리디렉션
    }

    @PostMapping("/setDefaultProfile")
    public String setDefaultProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        memberService.setDefaultProfile(username);

        return "redirect:/"; // 메인 화면으로 리디렉션
    }

    @PostMapping("/delete")
    @ResponseBody
    public String deleteMember(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        memberService.deleteMember(username);
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication()); // 로그아웃 처리

        return "success"; // 클라이언트에 성공 메시지 전송
    }

    // 여기까지

    @ToString
    @Getter
    @Setter
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

        private String providerTypeCode;

        private String role; // 권한 필드 추가
    }

    @ToString
    @Getter
    @Setter
    public static class GoogleSignForm {
        @NotBlank
        private String username;

        @NotBlank
        private String nickname;

        @NotBlank
        private String email;

        private String profileUrl;
    }

    // 필선
    // 레시피 노트 들어가기
    @ModelAttribute
    public void addCommonAttributes(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Member member = memberService.getMemberByUsername(username);
            model.addAttribute("currentMember", member);
        }
    }

  /*  @GetMapping("/{nickname}")
    public String showMemberProfilePage(@PathVariable(name = "nickname") String nickname, Model model) {
        Member member = rankingService.getMemberByNickname(nickname);
        List<Recipe> recipes = rankingService.getRecipesByNickname(nickname);

        model.addAttribute("member", member);
        model.addAttribute("recipes", recipes);

        return "ranking/member_profile"; // member_profile.html로 이동
    }*/

    // 비밀번호 변경
    @GetMapping("/editPw")
    public String editPasswordForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        model.addAttribute("editPasswordForm", new EditPasswordForm());
        return "member/editPw";
    }

    // 비밀번호 변경 처리

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EditPasswordForm {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;
    }


    @PostMapping("/editPw")
    public String editPasswordSubmit(@Valid @ModelAttribute EditPasswordForm editPasswordForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "member/editPw"; // 유효성 검사 오류 시 다시 비밀번호 변경 폼으로 이동
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 비밀번호 변경 로직 추가
        boolean changePasswordSuccess = memberService.changePassword(username, editPasswordForm.getCurrentPassword(), editPasswordForm.getNewPassword());

        if (!changePasswordSuccess) {
            model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
            return "member/editPw"; // 비밀번호 변경 실패 시 다시 비밀번호 변경 폼으로 이동
        }

        return "redirect:/"; // 비밀번호 변경 후 메인 화면으로 리디렉션
    }

}