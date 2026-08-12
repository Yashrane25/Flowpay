package com.yashrane.flowpay_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flowpayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FlowPay API")
                        .version("1.0")
                        .description("Digital wallet and peer-to-peer payment platform. " +
                                        "Authentication uses an httpOnly JWT cookie set by " +
                                        "POST /api/auth/login - log in via that endpoint first, " +
                                        "then other endpoints in this page will use your browser's " +
                                        "session cookie automatically."
                        )
                )
                .components(new Components().addSecuritySchemes("cookieAuth", new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("flowpay_jwt"))
                );
    }
}