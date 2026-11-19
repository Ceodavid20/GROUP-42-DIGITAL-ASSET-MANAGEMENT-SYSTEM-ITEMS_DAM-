package ng.edu.ui.items.dam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UiItemsDamApplication {
    public static void main(String[] args) {
        SpringApplication.run(UiItemsDamApplication.class, args);
    }
}
