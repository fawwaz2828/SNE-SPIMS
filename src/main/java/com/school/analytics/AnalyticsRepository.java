package com.school.analytics;

import com.school.util.MyBatis;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

/**
 * Read-only analytics queries for the dashboard. SQL lives in
 * {@code mappers/AnalyticsMapper.xml}; uses the shared {@link MyBatis} helper.
 */
@Repository
public class AnalyticsRepository {

    /** Average goal progress per domain per recorded date, scoped to the given students. */
    public List<DomainTrendRow> domainTrend(List<Long> studentIds, LocalDate from) {
        if (studentIds.isEmpty()) {
            return List.of();
        }
        return MyBatis.selectList("AnalyticsMapper.domainTrend",
                MyBatis.args("studentIds", studentIds, "fromDate", from));
    }

    // --- school-wide (admin) ---

    public List<CountRow> disabilityDistribution() {
        return MyBatis.selectList("AnalyticsMapper.disabilityDistribution");
    }

    public long activeIepCount() {
        Map<String, Object> none = MyBatis.args();
        return MyBatis.count("AnalyticsMapper.activeIepCount", none);
    }

    public long incidentsSince(LocalDate from) {
        return MyBatis.count("AnalyticsMapper.incidentsSince", MyBatis.args("fromDate", from));
    }
}
