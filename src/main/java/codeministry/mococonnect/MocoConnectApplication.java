package codeministry.mococonnect;

import codeministry.mococonnect.service.MocoConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@RequiredArgsConstructor
public class MocoConnectApplication implements CommandLineRunner {

    private final MocoConnectService mocoConnectService;

    public static void main(String[] args) {
        SpringApplication.run(MocoConnectApplication.class);
    }

    @Override
    public void run(final String... args) throws Exception {
        mocoConnectService.importTimeEntries();
    }
}
