package com.school.calendar;

/** A FullCalendar-shaped event (serialized to JSON by the /calendar/events endpoint). */
public class CalendarEvent {
    private String title;
    private String start;   // ISO date or date-time
    private String end;     // optional
    private boolean allDay;
    private String color;
    private String type;        // THERAPY | GOAL | SCHOOL
    private String description;

    public CalendarEvent(String title, String start, String end, boolean allDay,
                         String color, String type, String description) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
        this.color = color;
        this.type = type;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getStart() { return start; }
    public String getEnd() { return end; }
    public boolean isAllDay() { return allDay; }
    public String getColor() { return color; }
    public String getType() { return type; }
    public String getDescription() { return description; }
}
