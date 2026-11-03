package com.school.calendar;

import com.school.model.SchoolCalendarEvent;
import com.school.model.TherapySession;
import com.school.util.MyBatis;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CalendarRepository {

    public List<TherapySession> therapyInRange(List<Long> studentIds, LocalDate from, LocalDate to) {
        if (studentIds.isEmpty()) {
            return List.of();
        }
        return MyBatis.selectList("CalendarMapper.therapyInRange",
                MyBatis.args("studentIds", studentIds, "from", from.atStartOfDay(), "to", to.atStartOfDay()));
    }

    public List<GoalTargetRow> goalTargetsInRange(List<Long> studentIds, LocalDate from, LocalDate to) {
        if (studentIds.isEmpty()) {
            return List.of();
        }
        return MyBatis.selectList("CalendarMapper.goalTargetsInRange",
                MyBatis.args("studentIds", studentIds, "from", from, "to", to));
    }

    public List<SchoolCalendarEvent> schoolCalendarInRange(LocalDate from, LocalDate to) {
        return MyBatis.selectList("CalendarMapper.schoolCalendarInRange",
                MyBatis.args("from", from, "to", to));
    }
}
