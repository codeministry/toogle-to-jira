package codeministry.connect.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    @Value("${toggl-to-jira.settings.jira.user-email}")
    private String userEmail;

    @Value("${toggl-to-jira.settings.jira.api-token}")
    private String apiToken;

    @Bean
    public RestTemplate jiraRestTemplate() {
        BasicAuthenticationInterceptor basicAuthenticationInterceptor = new BasicAuthenticationInterceptor(this.userEmail, this.apiToken);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(basicAuthenticationInterceptor);

        return restTemplate;
    }
}
