package com.school.analytics;

import java.util.List;

/**
 * View model for the progress line chart: a shared list of date labels and one
 * data series per developmental domain. Serialized to JSON by Thymeleaf for Chart.js.
 */
public class TrendChart {

    private final List<String> labels;
    private final List<DomainSeries> series;

    public TrendChart(List<String> labels, List<DomainSeries> series) {
        this.labels = labels;
        this.series = series;
    }

    public List<String> getLabels() { return labels; }
    public List<DomainSeries> getSeries() { return series; }

    public boolean isEmpty() { return labels.isEmpty() || series.isEmpty(); }

    /** A single domain's line: aligned to the chart's shared labels (null = gap). */
    public static class DomainSeries {
        private final String domain;
        private final String label;
        private final List<Double> data;

        public DomainSeries(String domain, String label, List<Double> data) {
            this.domain = domain;
            this.label = label;
            this.data = data;
        }

        public String getDomain() { return domain; }
        public String getLabel() { return label; }
        public List<Double> getData() { return data; }
    }
}
