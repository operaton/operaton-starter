@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()};

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean("timerDuration")
    String timerDuration(@Value("${"${timer.escalation.duration:PT1H}"}") String timerDuration) {
        return timerDuration;
    }
}
