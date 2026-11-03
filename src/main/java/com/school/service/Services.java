package com.school.service;

import com.school.dao.*;
import com.school.dao.impl.*;

/**
 * Tiny service locator that wires the DAO implementations into the service
 * objects once and hands them to the controllers. In a Spring application this
 * would be the container; here a single eagerly-built singleton keeps the wiring
 * explicit and dependency-free.
 */
public final class Services {

    private static final Services INSTANCE = new Services();

    // DAOs
    private final UserDao userDao = new UserDaoImpl();
    private final StudentDao studentDao = new StudentDaoImpl();
    private final AssignmentDao assignmentDao = new AssignmentDaoImpl();
    private final IepDao iepDao = new IepDaoImpl();
    private final DailyReportDao reportDao = new DailyReportDaoImpl();
    private final IncidentDao incidentDao = new IncidentDaoImpl();
    private final TherapyDao therapyDao = new TherapyDaoImpl();
    private final MessageDao messageDao = new MessageDaoImpl();
    private final AnnouncementDao announcementDao = new AnnouncementDaoImpl();

    // Shared collaborators
    private final AccessControl accessControl = new AccessControl(assignmentDao);

    // Services
    private final AuthService authService = new AuthService(userDao);
    private final UserService userService = new UserService(userDao);
    private final StudentService studentService =
            new StudentService(studentDao, assignmentDao, accessControl);
    private final IepService iepService = new IepService(iepDao, accessControl);
    private final ReportService reportService = new ReportService(reportDao, accessControl);
    private final IncidentService incidentService = new IncidentService(incidentDao, accessControl);
    private final TherapyService therapyService = new TherapyService(therapyDao, accessControl);
    private final MessageService messageService =
            new MessageService(messageDao, assignmentDao, accessControl);
    private final AnnouncementService announcementService = new AnnouncementService(announcementDao);

    private Services() {
    }

    public static Services get() {
        return INSTANCE;
    }

    public AccessControl accessControl()       { return accessControl; }
    public AuthService auth()                  { return authService; }
    public UserService users()                 { return userService; }
    public StudentService students()           { return studentService; }
    public IepService iep()                    { return iepService; }
    public ReportService reports()             { return reportService; }
    public IncidentService incidents()         { return incidentService; }
    public TherapyService therapy()            { return therapyService; }
    public MessageService messages()           { return messageService; }
    public AnnouncementService announcements()  { return announcementService; }
}
