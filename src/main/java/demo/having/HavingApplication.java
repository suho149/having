package demo.having;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class HavingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HavingApplication.class, args);
    }

}
