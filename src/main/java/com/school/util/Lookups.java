package com.school.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central catalogue of the controlled vocabularies (the DB ENUM values) used to
 * populate dropdowns in the View layer and to validate input in the Service
 * layer. Keeping them in one place keeps the JSPs, services and schema aligned.
 */
public final class Lookups {

    private Lookups() {
    }

    public static final List<String> ROLES =
            Arrays.asList("ADMIN", "TEACHER", "PARENT");

    public static final List<String> GENDERS =
            Arrays.asList("MALE", "FEMALE", "OTHER");

    /** Disability classifications with human-friendly labels. */
    public static final Map<String, String> DISABILITY_TYPES = ordered(
            "AUTISM", "Autism Spectrum Disorder",
            "INTELLECTUAL", "Intellectual Disability",
            "HEARING", "Hearing Impairment",
            "VISUAL", "Visual Impairment",
            "PHYSICAL", "Physical Disability",
            "SPEECH_LANGUAGE", "Speech / Language Disorder",
            "LEARNING", "Specific Learning Disability",
            "MULTIPLE", "Multiple Disabilities",
            "OTHER", "Other");

    public static final List<String> SUPPORT_LEVELS =
            Arrays.asList("LOW", "MODERATE", "HIGH", "INTENSIVE");

    public static final List<String> RELATIONSHIPS =
            Arrays.asList("MOTHER", "FATHER", "GUARDIAN", "OTHER");

    /** The five+ developmental domains used by IEP goals. */
    public static final Map<String, String> DOMAINS = ordered(
            "ACADEMIC", "Academic",
            "COMMUNICATION", "Communication",
            "SOCIAL_EMOTIONAL", "Social / Emotional",
            "MOTOR", "Motor",
            "BEHAVIORAL", "Behavioral",
            "SELF_CARE", "Self-Care",
            "VOCATIONAL", "Vocational");

    public static final List<String> IEP_STATUS =
            Arrays.asList("DRAFT", "ACTIVE", "COMPLETED", "ARCHIVED");

    public static final List<String> GOAL_STATUS =
            Arrays.asList("NOT_STARTED", "IN_PROGRESS", "ACHIEVED", "DISCONTINUED");

    public static final List<String> MOODS =
            Arrays.asList("HAPPY", "CALM", "ANXIOUS", "FRUSTRATED", "TIRED");

    public static final List<String> RUBRIC_RATINGS =
            Arrays.asList("EMERGING", "DEVELOPING", "PROFICIENT", "MASTERED");

    /** Default rubric criteria captured on every daily report. */
    public static final List<String> RUBRIC_CRITERIA = Arrays.asList(
            "Academic Engagement",
            "Social Interaction",
            "Emotional Regulation",
            "Motor Skills",
            "Communication");

    public static final List<String> SEVERITIES =
            Arrays.asList("LOW", "MEDIUM", "HIGH", "CRITICAL");

    public static final List<String> THERAPY_TYPES =
            Arrays.asList("SPEECH", "OCCUPATIONAL", "PHYSICAL", "BEHAVIORAL", "COUNSELING");

    public static final List<String> THERAPY_STATUS =
            Arrays.asList("SCHEDULED", "COMPLETED", "CANCELLED", "NO_SHOW");

    public static final List<String> ANNOUNCEMENT_TARGETS =
            Arrays.asList("ALL", "TEACHER", "PARENT");

    /** Turns an ENUM_CODE into a "Enum Code" display label. */
    public static String label(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }
        String[] parts = code.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private static Map<String, String> ordered(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }
}
