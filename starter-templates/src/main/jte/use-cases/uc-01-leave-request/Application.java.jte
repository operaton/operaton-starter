@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()};

import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Clock;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Clock leaveRequestClock() {
        return Clock.systemDefaultZone();
    }
}
