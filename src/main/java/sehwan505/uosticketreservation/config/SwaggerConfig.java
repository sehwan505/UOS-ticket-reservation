package sehwan505.uosticketreservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버"),
                        new Server().url("https://api.uos-ticket.com").description("운영 서버")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info apiInfo() {
        return new Info()
                .title("UOS 영화 예매 시스템 API")
                .description("""
                        UOS 영화 예매 시스템의 REST API 문서입니다.
                        
                        ## 주요 기능
                        - 영화 정보 조회 및 관리
                        - 회원 가입 및 관리
                        - 영화 예매 및 결제
                        - 리뷰 작성 및 관리
                        - 은행 결제 시스템 연동
                        
                        ## 인증 방법
                        JWT 토큰을 사용하여 인증합니다. 로그인 후 받은 토큰을 'Bearer {token}' 형식으로 입력하세요.
                        
                        ## 문의사항
                        개발팀: dev@uos-ticket.com
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("UOS 개발팀")
                        .email("dev@uos-ticket.com")
                        .url("https://github.com/uos-ticket/backend")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }
} 