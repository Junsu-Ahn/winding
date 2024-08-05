package com.example.demo.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(new AntPathRequestMatcher("/oauth2/login/info")).authenticated() // 특정 경로에 대한 인증 설정
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
                        .anyRequest().permitAll() // 나머지 요청은 모두 허용
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/member/login") // 로그인 페이지 지정
                        .defaultSuccessUrl("/") // 로그인 성공 후 리다이렉트될 URL

                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/member/login")) // OAuth2 로그인 페이지 지정
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout")) // 로그아웃 요청 경로 지정
                        .logoutSuccessUrl("/") // 로그아웃 후 리다이렉트될 URL
                        .invalidateHttpSession(true) // 세션 무효화 설정
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/**"))) // CSRF 보호 비활성화
//                .csrf(csrf -> csrf
//                        .ignoringRequestMatchers("/oauth2/login/info", "/admin/**")) // CSRF 보호 제외 경로 설정
//                .addFilterBefore(new HiddenHttpMethodFilter(), CsrfFilter.class) // HTTP 메소드 허용 필터 추가
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
