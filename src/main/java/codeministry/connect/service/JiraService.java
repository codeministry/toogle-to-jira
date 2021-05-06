package codeministry.connect.service;

import codeministry.connect.dto.TimeEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class JiraService {
    @Value("${application.settings.jira.url}")
    private String url;

    @Value("${application.settings.jira.path}")
    private String path;

    private final RestTemplate jiraRestTemplate;

    public void addTimeEntry(final String issueId, final TimeEntry timeEntry) {
        String apiUrl = this.url.concat(this.path);
        apiUrl = apiUrl.replace("{issueId}", issueId);

        jiraRestTemplate.exchange(apiUrl, HttpMethod.POST, new HttpEntity<>(timeEntry), String.class);
    }
}
