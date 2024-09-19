package com.example.demo.member.service;

import com.example.demo.global.email.service.EmailService;
import com.example.demo.global.exception.DataNotFoundException;
import com.example.demo.global.exception.PasswordMismatchException;
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

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${custom.fileDirPath}")
    private String fileDirPath;

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://github.com/Junsu-Ahn/cookers/assets/134615615/5c0de0e0-b917-47ae-94d4-d8ad366dce7f";

    // 회원가입 메서드
    public Member signup(String address, String username, String password, String passwordConfirm, String nickname, String email, Long hit, String url) {
        if (!password.equals(passwordConfirm)) {
            throw new PasswordMismatchException("비밀번호가 서로 다릅니다.");
        }
        if (memberRepository.existsByUsername(username)) {
            throw new DataIntegrityViolationException("이미 존재하는 아이디입니다.");
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new DataIntegrityViolationException("이미 존재하는 닉네임입니다.");
        }

        Member member = Member.builder()
                .address(address)
                .username(username)
                .password(passwordEncoder.encode(password))
                .profileImg(url)
                .nickname(nickname)
                .email(email)
                .role(Role.ROLE_USER)  // 기본 권한 USER
                .build();

        return memberRepository.save(member);
    }

    // 관리자 계정 생성
    @Transactional
    public void createAdmin() {
        if (memberRepository.findByUsername("admin").isEmpty()) {
            Member admin = Member.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .nickname("Admin")
                    .email("admin@example.com")
                    .address("Admin Address")
                    .role(Role.ROLE_ADMIN)
                    .build();
            memberRepository.save(admin);
        }
    }

    // 소셜 로그인 처리
    @Transactional
    public Member whenSocialLogin(String address, String username, String nickname, String profileImageUrl, String email) {
        return memberRepository.findByUsername(username)
                .orElseGet(() -> signup(address, username, "", "", nickname, email, 0L, profileImageUrl));
    }

    // 관리자에 의한 회원 삭제
    @Transactional
    public void deleteMemberByAdmin(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. ID: " + username));
        memberRepository.delete(member);
    }

    // 사용자 이름으로 회원 조회
    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    // 이메일로 회원 조회
    public List<Member> findByUserEmail(String email) {
        return memberRepository.findByemail(email);
    }

    // 전체 회원 목록 조회
    public List<Member> getAllMembers() {
        return Optional.ofNullable(memberRepository.findAll()).orElse(Collections.emptyList());
    }

    // 비밀번호 인증
    public boolean authenticateMember(String username, String password) {
        return memberRepository.findByUsername(username)
                .map(member -> passwordEncoder.matches(password, member.getPassword()))
                .orElse(false);
    }

    // 비밀번호 변경
    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            return false;
        }

        // 새로운 비밀번호 설정
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
        return true;
    }

    // 회원 정보 업데이트 (닉네임 및 이메일)
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

    // 기본 프로필 이미지로 변경
    @Transactional
    public void setDefaultProfile(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        member.setProfileImg(DEFAULT_PROFILE_IMAGE_URL);
        memberRepository.save(member);
    }

    // 회원 삭제
    @Transactional
    public void deleteMember(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        memberRepository.delete(member);
    }

    // 파일 저장
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

    public Member save(Member member) {
        // Member 객체를 저장 (새로 추가되거나 기존 데이터 업데이트)
        return memberRepository.save(member);
    }
}