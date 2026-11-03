package com.school.analytics;

/** A label/count pair, e.g. disability type distribution. */
public class CountRow {
    private String label;
    private long total;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
