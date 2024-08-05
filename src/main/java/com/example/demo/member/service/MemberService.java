package com.example.demo.member.service;

import com.example.demo.global.email.service.EmailService;
import com.example.demo.global.exception.DataNotFoundException;
import com.example.demo.global.exception.PasswordMismatchException;
import com.example.demo.global.exception.ResourceNotFoundException;
import com.example.demo.member.entity.Member;
import com.example.demo.member.entity.Role;
import com.example.demo.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor

public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    @Value("${custom.fileDirPath}")
    private String fileDirPath;

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://github.com/Junsu-Ahn/cookers/assets/134615615/5c0de0e0-b917-47ae-94d4-d8ad366dce7f";

    public Member signup(String address, String username, String password, String passwordConfirm, String nickname, String email, Long hit, String url) {

        if (!password.equals(passwordConfirm)) {
            throw new PasswordMismatchException("비밀번호가 서로 다릅니다.");
        }

        if (memberRepository.existsByUsername(username)) {
            throw new DataIntegrityViolationException("이미 존재하는 아이디입니다.");
        }

        // 중복 닉네임 확인
        if (memberRepository.existsByNickname(nickname)) {
            throw new DataIntegrityViolationException("이미 존재하는 닉네임입니다.");
        }

        Member member = Member
                .builder()
                .address(address)
                .username(username)
                .password(passwordEncoder.encode(password))
                .profileImg(url)
                .nickname(nickname)
                .email(email)

                .role(Role.ROLE_USER)  // 기본적으로 USER 권한을 부여
                .build();

        emailService.send(email, "Winding 회원가입을 축하합니다!", "Winding 회원가입이 정상적으로 완료되었습니다^^~!");
        return memberRepository.save(member);
    }

    @Transactional
    public Member createAdmin(String username, String password) {
        if (memberRepository.existsByUsername(username)) {
            throw new DataIntegrityViolationException("이미 존재하는 아이디입니다.");
        }

        Member admin = Member.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(Role.ROLE_ADMIN)
                .build();

        return memberRepository.save(admin);
    }

    @Transactional
    public Member whenSocialLogin(String address, String username, String nickname, String profileImageUrl, String email) {
        Optional<Member> opMember = findByUsername(username);

        if (opMember.isPresent()) return opMember.get();

        // 소셜 로그인를 통한 가입시 비번은 없다.
        return signup("", username,  "", "",nickname, email, 0L, profileImageUrl); // 최초 로그인 시 딱 한번 실행
    }

    @Transactional
    public void deleteMemberByAdmin(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. ID: " + username));
        memberRepository.delete(member);
    }


    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public List<Member> findByUserEmail(String email) {
        return memberRepository.findByemail(email);
    }
    public List<Member> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members != null ? members : Collections.emptyList();
    }

    public boolean authenticateMember(String username, String password) {
        Optional<Member> memberOptional = memberRepository.findByUsername(username);
        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            return passwordEncoder.matches(password, member.getPassword());
        }
        return false;
    }

    public Member getMember(String username) {
        Optional<Member> member = this.memberRepository.findByUsername(username);
        if (member.isPresent()) {
            return member.get();
        } else {
            throw new DataNotFoundException("member not found");
        }
    }

    public Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow();
    }


    /*@Transactional(readOnly = true)
    public Long calculateTotalHitsForMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return 0L;
        }

        Set<RecipeRecommendation> recipeRecommendations = member.getRecipeRecommendations();
        Long totalHits = 0L;
        for (RecipeRecommendation recommendation : recipeRecommendations) {
            Recipe recipe = recommendation.getRecipe();
            if (recipe != null) {
                totalHits += recipe.getHit();
            }
        }
        return totalHits;
    }*/

    public void save(Member member) {
        memberRepository.save(member);
    }

    public void delete(Member member) {memberRepository.delete(member);}

    // 추가//


    @Transactional
    public Member updateMember(String username, String nickname, String email, MultipartFile profileImg) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        member.setNickname(nickname);
        member.setEmail(email);

        if (profileImg != null && !profileImg.isEmpty()) {
            String fileUrl = saveFile(profileImg);
            member.setProfileImg(fileUrl);
        }

        return memberRepository.save(member);
    }

    @Transactional
    public void setDefaultProfile(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        member.setProfileImg(DEFAULT_PROFILE_IMAGE_URL);
        memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        memberRepository.delete(member);
    }

    private String saveFile(MultipartFile file) {
        try {
            Path rootLocation = Paths.get(fileDirPath);
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
            String filename = System.currentTimeMillis() + "-" + file.getOriginalFilename();
            Path destinationFile = rootLocation.resolve(filename);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return "/imagefile/post/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    public Optional<Member> findByusername(String username) {
        return memberRepository.findByUsername(username);
    }

    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            return false; // 현재 비밀번호가 일치하지 않는 경우 변경 실패
        }

        // 새로운 비밀번호 설정
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
        return true; // 비밀번호 변경 성공
    }
    //여기까지
}