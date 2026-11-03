package com.school.service;

import com.school.dao.IepDao;
import com.school.model.IepGoal;
import com.school.model.IepProgram;
import com.school.model.User;
import com.school.util.Lookups;
import com.school.util.MyBatis;
import com.school.util.ValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** IEP program and goal management with domain grouping for the detail view. */
public class IepService {

    private final IepDao iepDao;
    private final AccessControl access;

    public IepService(IepDao iepDao, AccessControl access) {
        this.iepDao = iepDao;
        this.access = access;
    }

    public List<IepProgram> programsForStudent(User user, long studentId) {
        access.require(user, studentId);
        return iepDao.programsForStudent(studentId);
    }

    /** Loads a program with its goals, enforcing access through the student. */
    public IepProgram getProgram(User user, long programId) {
        IepProgram p = iepDao.findProgram(programId)
                .orElseThrow(() -> new ValidationException("IEP not found."));
        access.require(user, p.getStudentId());
        p.setGoals(iepDao.goalsForProgram(programId));
        applyTimeProgress(p);
        return p;
    }

    /**
     * Each goal's progress is DERIVED from time, not entered by hand: it grows from
     * 0% at the program's start date to 100% on the goal's target date, based on
     * today. Goals without a target date keep their stored value. Applied on read
     * so the IEP page and the parent dashboard's "Progress by area" stay in sync.
     */
    private static void applyTimeProgress(IepProgram p) {
        LocalDate today = LocalDate.now();
        for (IepGoal g : p.getGoals()) {
            Integer pct = timeProgress(p.getStartDate(), g.getTargetDate(), today);
            if (pct != null) {
                g.setProgressPct(pct);
            }
        }
    }

    /** Percentage of the start→target window that has elapsed by {@code today}, 0–100. */
    private static Integer timeProgress(LocalDate start, LocalDate target, LocalDate today) {
        if (start == null || target == null) {
            return null;
        }
        if (!target.isAfter(start)) {
            return today.isBefore(target) ? 0 : 100;
        }
        if (!today.isAfter(start)) {
            return 0;
        }
        if (!today.isBefore(target)) {
            return 100;
        }
        long total = ChronoUnit.DAYS.between(start, target);
        long elapsed = ChronoUnit.DAYS.between(start, today);
        return (int) Math.round(elapsed * 100.0 / total);
    }

    /** Goals grouped by developmental domain (preserves Lookups.DOMAINS order). */
    public Map<String, List<IepGoal>> goalsByDomain(IepProgram program) {
        Map<String, List<IepGoal>> grouped = new LinkedHashMap<>();
        for (String domain : Lookups.DOMAINS.keySet()) {
            grouped.put(domain, new java.util.ArrayList<>());
        }
        for (IepGoal g : program.getGoals()) {
            grouped.computeIfAbsent(g.getDomain(), k -> new java.util.ArrayList<>()).add(g);
        }
        grouped.values().removeIf(List::isEmpty);
        return grouped;
    }

    public long createProgram(IepProgram p) {
        if (isBlank(p.getTitle())) {
            throw new ValidationException("IEP title is required.");
        }
        if (isBlank(p.getAcademicYear())) {
            throw new ValidationException("Academic year is required.");
        }
        if (p.getStartDate() == null || p.getEndDate() == null) {
            throw new ValidationException("Start and end dates are required.");
        }
        if (p.getEndDate().isBefore(p.getStartDate())) {
            throw new ValidationException("End date must be after the start date.");
        }
        if (iepDao.yearExists(p.getStudentId(), p.getAcademicYear(), 0)) {
            throw new ValidationException("An IEP for " + p.getAcademicYear() + " already exists for this student.");
        }
        if (!Lookups.IEP_STATUS.contains(p.getStatus())) {
            p.setStatus("DRAFT");
        }
        return iepDao.insertProgram(p);
    }

    public void updateProgram(IepProgram p) {
        if (iepDao.yearExists(p.getStudentId(), p.getAcademicYear(), p.getId())) {
            throw new ValidationException("Another IEP for " + p.getAcademicYear() + " already exists.");
        }
        iepDao.updateProgram(p);
    }

    public void addGoal(User recorder, IepGoal g) {
        if (!Lookups.DOMAINS.containsKey(g.getDomain())) {
            throw new ValidationException("Invalid developmental domain.");
        }
        if (isBlank(g.getDescription())) {
            throw new ValidationException("Goal description is required.");
        }
        if (!Lookups.GOAL_STATUS.contains(g.getStatus())) {
            g.setStatus("NOT_STARTED");
        }
        clampProgress(g);
        // Goal and its seed snapshot persist together. The snapshot history is
        // captured intentionally (audit trail / future progress-trend chart); it
        // is write-only for now, so it is not dead code.
        MyBatis.tx(() -> {
            iepDao.insertGoal(g);
            iepDao.insertProgressSnapshot(g.getId(), g.getProgressPct(), recorder.getId());
        });
    }

    public void updateGoal(User recorder, IepGoal g) {
        if (isBlank(g.getDescription())) {
            throw new ValidationException("Goal description is required.");
        }
        clampProgress(g);
        // Goal update and its progress snapshot persist together (D6).
        MyBatis.tx(() -> {
            iepDao.updateGoal(g);
            iepDao.insertProgressSnapshot(g.getId(), g.getProgressPct(), recorder.getId());
        });
    }

    public void deleteGoal(long goalId) {
        iepDao.deleteGoal(goalId);
    }

    public IepGoal getGoal(long goalId) {
        return iepDao.findGoal(goalId)
                .orElseThrow(() -> new ValidationException("Goal not found."));
    }

    private static void clampProgress(IepGoal g) {
        if (g.getProgressPct() < 0) g.setProgressPct(0);
        if (g.getProgressPct() > 100) g.setProgressPct(100);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
