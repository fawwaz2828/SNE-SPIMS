package com.school;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SNE-SPIMS — Spring Boot 3 entry point.
 *
 * <p>The data layer (MyBatis + its own HikariCP pool, see {@code com.school.util.Db})
 * is self-contained and configured from {@code db.properties}, so Spring's
 * {@link DataSourceAutoConfiguration} is excluded — Spring does not manage the
 * datasource. {@link EnableScheduling} powers the therapy-reminder cron and
 * {@link EnableAsync} the event-driven notifications.</p>
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableScheduling
@EnableAsync
public class SneSpimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SneSpimsApplication.class, args);
    }
}
