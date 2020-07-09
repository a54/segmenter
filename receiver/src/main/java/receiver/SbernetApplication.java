package receiver;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;


@SpringBootApplication(exclude = {JmxAutoConfiguration.class, JtaAutoConfiguration.class})
@RequiredArgsConstructor
public class SbernetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbernetApplication.class);
    }

}
