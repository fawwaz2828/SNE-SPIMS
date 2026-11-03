package com.school.analytics;

import java.util.List;

/** Everything the dashboard view needs, assembled per-role by {@link AnalyticsService}. */
public class DashboardData {

    // Progress trend (all roles)
    private TrendChart trend;

    // Admin school-wide stats
    private boolean showAdminStats;
    private List<CountRow> disabilityDistribution;
    private long activeIepCount;
    private long incidents4Weeks;
    private double incidentsPerWeek;

    public TrendChart getTrend() { return trend; }
    public void setTrend(TrendChart trend) { this.trend = trend; }

    public boolean isShowAdminStats() { return showAdminStats; }
    public void setShowAdminStats(boolean v) { this.showAdminStats = v; }

    public List<CountRow> getDisabilityDistribution() { return disabilityDistribution; }
    public void setDisabilityDistribution(List<CountRow> v) { this.disabilityDistribution = v; }

    public long getActiveIepCount() { return activeIepCount; }
    public void setActiveIepCount(long v) { this.activeIepCount = v; }

    public long getIncidents4Weeks() { return incidents4Weeks; }
    public void setIncidents4Weeks(long v) { this.incidents4Weeks = v; }

    public double getIncidentsPerWeek() { return incidentsPerWeek; }
    public void setIncidentsPerWeek(double v) { this.incidentsPerWeek = v; }
}
