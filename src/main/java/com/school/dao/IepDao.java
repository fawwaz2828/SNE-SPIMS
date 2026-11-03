package com.school.dao;

import com.school.model.IepGoal;
import com.school.model.IepProgram;

import java.util.List;
import java.util.Optional;

/** Data access for IEP programs and their goals. */
public interface IepDao {

    long insertProgram(IepProgram p);

    void updateProgram(IepProgram p);

    Optional<IepProgram> findProgram(long id);

    /** All IEPs for a student, newest academic year first. */
    List<IepProgram> programsForStudent(long studentId);

    /** The single active IEP for a student, if any (parent view). */
    Optional<IepProgram> activeProgramForStudent(long studentId);

    boolean yearExists(long studentId, String academicYear, long excludeId);

    // goals
    long insertGoal(IepGoal g);

    void updateGoal(IepGoal g);

    Optional<IepGoal> findGoal(long id);

    List<IepGoal> goalsForProgram(long iepId);

    void deleteGoal(long goalId);

    // goal progress history (D6)
    void insertProgressSnapshot(long goalId, int progressPct, long recordedBy);
}
