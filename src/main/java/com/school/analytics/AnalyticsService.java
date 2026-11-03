package com.school.analytics;

import com.school.model.Student;
import com.school.model.User;
import com.school.util.Lookups;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

/** Assembles the dashboard analytics, scoped to the caller's role and filters. */
@Service
public class AnalyticsService {

    private final AnalyticsRepository repo;

    public AnalyticsService(AnalyticsRepository repo) {
        this.repo = repo;
    }

    /** Period filter → look-back window in days. */
    public static int periodDays(String period) {
        if (period == null) {
            return 30;
        }
        switch (period.toUpperCase()) {
            case "KUARTAL": return 90;
            case "SEMESTER": return 180;
            default: return 30; // BULAN
        }
    }

    /**
     * @param visible     the students the user may see (already access-scoped)
     * @param studentId   optional single-student filter (parent/teacher view)
     */
    public DashboardData build(User user, List<Student> visible, String period, Long studentId) {
        int days = periodDays(period);
        LocalDate from = LocalDate.now().minusDays(days);

        List<Long> studentIds = resolveStudentIds(visible, studentId);

        DashboardData data = new DashboardData();
        data.setTrend(buildTrend(repo.domainTrend(studentIds, from)));

        if (user.isAdmin()) {
            data.setShowAdminStats(true);
            data.setDisabilityDistribution(repo.disabilityDistribution());
            data.setActiveIepCount(repo.activeIepCount());
            LocalDate fourWeeks = LocalDate.now().minusDays(28);
            long incidents = repo.incidentsSince(fourWeeks);
            data.setIncidents4Weeks(incidents);
            data.setIncidentsPerWeek(Math.round((incidents / 4.0) * 10.0) / 10.0);
        }
        return data;
    }

    private List<Long> resolveStudentIds(List<Student> visible, Long studentId) {
        List<Long> all = new ArrayList<>();
        for (Student s : visible) {
            all.add(s.getId());
        }
        if (studentId != null && all.contains(studentId)) {
            return List.of(studentId);
        }
        return all;
    }

    /** Pivots flat (domain, date, value) rows into a labels + per-domain series chart. */
    private TrendChart buildTrend(List<DomainTrendRow> rows) {
        // Shared, sorted date axis.
        TreeSet<LocalDate> dates = new TreeSet<>();
        // domain -> (date -> value)
        Map<String, Map<LocalDate, Double>> byDomain = new LinkedHashMap<>();
        for (DomainTrendRow r : rows) {
            dates.add(r.getRecordedOn());
            byDomain.computeIfAbsent(r.getDomain(), k -> new LinkedHashMap<>())
                    .put(r.getRecordedOn(), r.getAvgProgress());
        }

        List<LocalDate> axis = new ArrayList<>(dates);
        List<String> labels = new ArrayList<>();
        for (LocalDate d : axis) {
            labels.add(d.toString());
        }

        List<TrendChart.DomainSeries> series = new ArrayList<>();
        // Preserve the canonical domain order from Lookups.
        for (String domain : Lookups.DOMAINS.keySet()) {
            Map<LocalDate, Double> points = byDomain.get(domain);
            if (points == null) {
                continue;
            }
            List<Double> values = new ArrayList<>();
            for (LocalDate d : axis) {
                values.add(points.get(d)); // null leaves a gap in the line
            }
            series.add(new TrendChart.DomainSeries(domain, Lookups.DOMAINS.get(domain), values));
        }
        return new TrendChart(labels, series);
    }
}
