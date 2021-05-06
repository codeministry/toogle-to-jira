package codeministry.connect;

import codeministry.connect.service.TogglService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class TogglToJiraApplication implements CommandLineRunner {

    private final TogglService togglService;

    public static void main(String[] args) {
        SpringApplication.run(TogglToJiraApplication.class);
    }

    @Override
    public void run(final String... args) throws Exception {
        togglService.importTimeEntries();
    }
}
