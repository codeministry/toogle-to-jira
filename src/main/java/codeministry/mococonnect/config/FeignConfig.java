package codeministry.mococonnect.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Value("${application.settings.moco.token}")
    private String token;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> template.header("Authorization", "Token " + token);
    }
}
