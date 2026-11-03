package com.school.service;

import com.school.dao.IncidentDao;
import com.school.model.BehaviorIncident;
import com.school.model.User;
import com.school.util.Lookups;
import com.school.util.ValidationException;

import java.time.LocalDate;
import java.util.List;

/**
 * Behavioural incident logging. Role-scoped views: admin sees everything,
 * teachers see their caseload, parents see only high-severity or explicitly
 * shared incidents (filtered in the DAO query, never fetched then hidden).
 */
public class IncidentService {

    private final IncidentDao incidentDao;
    private final AccessControl access;

    public IncidentService(IncidentDao incidentDao, AccessControl access) {
        this.incidentDao = incidentDao;
        this.access = access;
    }

    public List<BehaviorIncident> view(User user, Long studentId, String severity,
                                       LocalDate from, LocalDate to) {
        switch (user.getRole()) {
            case "ADMIN":
                return incidentDao.search(studentId, severity, from, to);
            case "TEACHER":
                return incidentDao.forTeacher(user.getId(), severity, from, to);
            case "PARENT":
                if (studentId == null) {
                    throw new ValidationException("A child must be selected.");
                }
                access.require(user, studentId);
                return incidentDao.visibleToParentForStudent(studentId);
            default:
                throw new ValidationException("Unknown role.");
        }
    }

    public BehaviorIncident get(User user, long id) {
        BehaviorIncident i = incidentDao.find(id)
                .orElseThrow(() -> new ValidationException("Incident not found."));
        access.require(user, i.getStudentId());
        if (user.isParent() && !(i.isParentVisible() || isHigh(i.getSeverity()))) {
            throw new ValidationException("Incident not found.");
        }
        return i;
    }

    public long log(User teacher, BehaviorIncident i) {
        access.require(teacher, i.getStudentId());
        if (i.getIncidentDate() == null) {
            throw new ValidationException("Incident date/time is required.");
        }
        if (isBlank(i.getDescription())) {
            throw new ValidationException("A description is required.");
        }
        if (!Lookups.SEVERITIES.contains(i.getSeverity())) {
            throw new ValidationException("Invalid severity.");
        }
        // High and critical incidents are always parent-visible.
        if (isHigh(i.getSeverity())) {
            i.setParentVisible(true);
        }
        i.setReportedBy(teacher.getId());
        return incidentDao.insert(i);
    }

    private static boolean isHigh(String severity) {
        return "HIGH".equals(severity) || "CRITICAL".equals(severity);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
