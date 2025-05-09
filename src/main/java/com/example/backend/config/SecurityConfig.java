package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(
                                "/nonmember",         // 공개 접근 허용 test
                                "/",                  // 홈
                                "/css/**",            // 정적 리소스
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated() // 그 외는 인증 필요
                )
                .formLogin((form) -> form
                        .loginPage("/login")      // 커스텀 로그인 페이지 URL (추후 제작 예정)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/")    // 로그아웃 후 이동할 페이지
                        .permitAll()
                );

        return http.build();
    }
}