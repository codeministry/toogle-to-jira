package codeministry.connect;

import codeministry.connect.service.ApiConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@RequiredArgsConstructor
public class ConnectApplication implements CommandLineRunner {

    private final ApiConnectService apiConnectService;

    public static void main(String[] args) {
        SpringApplication.run(ConnectApplication.class);
    }

    @Override
    public void run(final String... args) throws Exception {
        apiConnectService.importTimeEntries();
    }
}
