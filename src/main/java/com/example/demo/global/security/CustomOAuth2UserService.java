package com.example.demo.global.security;



import com.example.demo.member.entity.Member;
import com.example.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    // 카카오톡 로그인이 성공할 때 마다 이 함수가 실행된다.
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        if (providerTypeCode.equals("KAKAO")) { // 카카오 로그인인 경우
            String oauthId = oAuth2User.getName();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Map attributesProperties = (Map) attributes.get("properties");
            String nickname = (String) attributesProperties.get("nickname");
            String profileImageUrl = (String) attributesProperties.get("profile_image");
            String email = (String) ((Map<String, Object>) attributes.get("kakao_account")).get("email");
            String username = providerTypeCode + "__%s".formatted(oauthId);

            Optional<Member> existingMember = memberService.findByUsername(username);
            if (existingMember.isPresent()) {
                return new CustomOAuth2User(existingMember.get().getUsername(), existingMember.get().getPassword(), new ArrayList<>());
            }

            // 새로운 회원 생성
            Member member = memberService.whenSocialLogin(providerTypeCode, username, nickname, profileImageUrl, email);

            List<GrantedAuthority> authorityList = new ArrayList<>();

            return new CustomOAuth2User(member.getUsername(), member.getPassword(), authorityList);
        } else if (providerTypeCode.equals("GOOGLE")) { // 구글 로그인인 경우
            // 여기에 구글 로그인 처리 코드를 추가하세요.
            // 구글 로그인 시에는 구글에서 제공하는 고유 사용자 식별자(sub), 이메일, 이름, 프로필 이미지 URL 등을 활용하여 회원 정보를 처리합니다.
            // 예시로 구글 로그인 처리 로직을 추가해보겠습니다.
            String oauthId = oAuth2User.getAttribute("sub");
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String profileImageUrl = oAuth2User.getAttribute("picture");

            String username = providerTypeCode + "__%s".formatted(oauthId);

            Optional<Member> existingMember = memberService.findByUsername(username);
            if (existingMember.isPresent()) {
                return new CustomOAuth2User(existingMember.get().getUsername(), existingMember.get().getPassword(), new ArrayList<>());
            }

            // 새로운 회원 생성
            Member member = memberService.whenSocialLogin(providerTypeCode, username, name, profileImageUrl, email);

            List<GrantedAuthority> authorityList = new ArrayList<>();

            return new CustomOAuth2User(member.getUsername(), member.getPassword(), authorityList);
        }else if (providerTypeCode.equals("NAVER")) { // 네이버 로그인인 경우
            // OAuth2User에서 "response" 속성을 가져옵니다.
            Map<String, Object> responseAttributes = (Map<String, Object>) oAuth2User.getAttribute("response");

            // "response" 속성이 존재하지 않거나 비어있는 경우 사용자 정보를 가져올 수 없으므로 예외를 던집니다.
            if (responseAttributes == null || responseAttributes.isEmpty()) {
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_response", "No response attributes found for Naver login.", null));
            }

            // "response" 속성에서 필요한 정보를 추출합니다.
            String oauthId = (String) responseAttributes.get("id");
            String nickname = (String) responseAttributes.get("nickname");
            String profileImageUrl = (String) responseAttributes.get("profile_image");
            String email = (String) responseAttributes.get("email");
            // 사용자의 고유한 username을 생성합니다.
            String username = providerTypeCode + "__%s".formatted(oauthId);

            // 생성된 username으로 회원을 찾습니다.
            Optional<Member> existingMember = memberService.findByUsername(username);

            // 이미 존재하는 회원인 경우 해당 회원 정보로 CustomOAuth2User를 생성하여 반환합니다.
            if (existingMember.isPresent()) {
                return new CustomOAuth2User(existingMember.get().getUsername(), existingMember.get().getPassword(), new ArrayList<>());
            }

            // 새로운 회원인 경우 회원을 생성합니다.
            Member member = memberService.whenSocialLogin(providerTypeCode, username, nickname, profileImageUrl, email);


            // 새로운 회원의 권한 목록을 생성합니다. (현재는 빈 목록)
            List<GrantedAuthority> authorityList = new ArrayList<>();

            // 생성된 CustomOAuth2User를 반환합니다.
            return new CustomOAuth2User(member.getUsername(), member.getPassword(), authorityList);
        }

        // 처리되지 않은 다른 OAuth2 로그인인 경우
        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider", "Unsupported OAuth2 provider: " + providerTypeCode, null));
    }
}


class CustomOAuth2User extends User implements OAuth2User {

    public CustomOAuth2User(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public String getName() {
        return getUsername();
    }
}