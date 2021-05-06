package codeministry.connect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {
    @Value("${application.settings.jira.user-email}")
    private String userEmail;

    @Value("${application.settings.jira.api-key}")
    private String apiKey;

    @Bean
    public RestTemplate jiraRestTemplate() {
        RestTemplate jiraRestTemplate = new RestTemplate();
        jiraRestTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(this.userEmail, this.apiKey));

        return jiraRestTemplate;
    }
}
