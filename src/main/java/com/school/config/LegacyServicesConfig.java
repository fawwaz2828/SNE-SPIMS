package com.school.config;

import com.school.dao.UserDao;
import com.school.dao.impl.UserDaoImpl;
import com.school.service.AccessControl;
import com.school.service.AnnouncementService;
import com.school.service.AuthService;
import com.school.service.IepService;
import com.school.service.IncidentService;
import com.school.service.MessageService;
import com.school.service.ReportService;
import com.school.service.Services;
import com.school.service.StudentService;
import com.school.service.TherapyService;
import com.school.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bridges the existing hand-wired {@link Services} locator into the Spring
 * context so controllers can {@code @Autowire} the business services without
 * rewriting the DAO/service layer. Each bean simply delegates to the singleton
 * locator, preserving the original wiring and the standalone MyBatis data layer.
 */
@Configuration
public class LegacyServicesConfig {

    private final Services services = Services.get();

    @Bean
    public AccessControl accessControl() {
        return services.accessControl();
    }

    @Bean
    public AuthService authService() {
        return services.auth();
    }

    @Bean
    public UserService userService() {
        return services.users();
    }

    @Bean
    public StudentService studentService() {
        return services.students();
    }

    @Bean
    public IepService iepService() {
        return services.iep();
    }

    @Bean
    public ReportService reportService() {
        return services.reports();
    }

    @Bean
    public IncidentService incidentService() {
        return services.incidents();
    }

    @Bean
    public TherapyService therapyService() {
        return services.therapy();
    }

    @Bean
    public MessageService messageService() {
        return services.messages();
    }

    @Bean
    public AnnouncementService announcementService() {
        return services.announcements();
    }

    /** Exposed directly for the security {@code UserDetailsService}. */
    @Bean
    public UserDao userDao() {
        return new UserDaoImpl();
    }

    /** Exposed for notification fan-out (parent lookup). */
    @Bean
    public com.school.dao.AssignmentDao assignmentDao() {
        return new com.school.dao.impl.AssignmentDaoImpl();
    }
}
