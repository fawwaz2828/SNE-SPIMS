package com.school.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.school.dao.AssignmentDao;
import com.school.dao.DailyReportDao;
import com.school.model.DailyReport;
import com.school.model.User;
import com.school.util.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ReportService")
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DailyReportDao reportDao;
    @Mock
    private AssignmentDao assignments; // backs a REAL AccessControl (only interfaces are mocked)

    private ReportService service;

    private static final long REPORT_ID = 7L;
    private static final long STUDENT_ID = 100L;
    private static final long PARENT_ID = 3L;
    private static final long TEACHER_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new ReportService(reportDao, new AccessControl(assignments));
    }

    private User user(long id, String role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    private DailyReport report(String status) {
        DailyReport r = new DailyReport();
        r.setId(REPORT_ID);
        r.setStudentId(STUDENT_ID);
        r.setTeacherId(TEACHER_ID);
        r.setStatus(status);
        return r;
    }

    @Nested
    @DisplayName("getWithRubrics()")
    class GetWithRubrics {

        @Test
        @DisplayName("hides an unpublished report from a parent")
        void parentCannotSeeDraft() {
            when(assignments.isParentOfStudent(PARENT_ID, STUDENT_ID)).thenReturn(true);
            when(reportDao.findReport(REPORT_ID)).thenReturn(Optional.of(report("DRAFT")));

            assertThatThrownBy(() -> service.getWithRubrics(user(PARENT_ID, "PARENT"), REPORT_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("lets staff read a draft, loading its rubrics")
        void staffCanReadDraft() {
            when(assignments.isTeacherOfStudent(TEACHER_ID, STUDENT_ID)).thenReturn(true);
            when(reportDao.findReport(REPORT_ID)).thenReturn(Optional.of(report("DRAFT")));
            when(reportDao.rubricsForReport(REPORT_ID)).thenReturn(List.of());
            when(reportDao.latestAcknowledgement(REPORT_ID)).thenReturn(Optional.empty());

            DailyReport result = service.getWithRubrics(user(TEACHER_ID, "TEACHER"), REPORT_ID);

            assertThat(result).isNotNull();
            assertThat(result.getRubrics()).isEmpty();
        }
    }

    @Nested
    @DisplayName("createDraft()")
    class CreateDraft {

        @Test
        @DisplayName("rejects an invalid rating before writing anything")
        void invalidRatingWritesNothing() {
            when(assignments.isTeacherOfStudent(TEACHER_ID, STUDENT_ID)).thenReturn(true);
            DailyReport r = report("DRAFT");
            r.setReportDate(java.time.LocalDate.now());
            r.setSummary("Good day.");

            assertThatThrownBy(() -> service.createDraft(user(TEACHER_ID, "TEACHER"), r,
                    Map.of("Academic Engagement", "NOT_A_RATING"), Map.of()))
                    .isInstanceOf(ValidationException.class);

            // Validation must happen before the report row is inserted (no orphan draft).
            verify(reportDao, never()).insertReport(r);
        }

        @Test
        @DisplayName("rejects a parent trying to author")
        void parentCannotAuthor() {
            DailyReport r = report("DRAFT");
            assertThatThrownBy(() -> service.createDraft(user(PARENT_ID, "PARENT"), r, Map.of(), Map.of()))
                    .isInstanceOf(com.school.util.AccessDeniedException.class);
            verify(reportDao, never()).insertReport(r);
        }
    }

    @Nested
    @DisplayName("acknowledge()")
    class Acknowledge {

        @Test
        @DisplayName("rejects a non-parent (e.g. teacher)")
        void rejectsNonParent() {
            assertThatThrownBy(() -> service.acknowledge(user(TEACHER_ID, "TEACHER"), REPORT_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("parents");

            verify(reportDao, never()).acknowledge(REPORT_ID, TEACHER_ID);
        }

        @Test
        @DisplayName("records a parent's acknowledgement of a published report")
        void parentAcknowledgesPublished() {
            when(assignments.isParentOfStudent(PARENT_ID, STUDENT_ID)).thenReturn(true);
            when(reportDao.findReport(REPORT_ID)).thenReturn(Optional.of(report("PUBLISHED")));
            when(reportDao.rubricsForReport(REPORT_ID)).thenReturn(List.of());
            when(reportDao.acknowledgedAt(REPORT_ID, PARENT_ID)).thenReturn(Optional.empty());

            service.acknowledge(user(PARENT_ID, "PARENT"), REPORT_ID);

            verify(reportDao).acknowledge(REPORT_ID, PARENT_ID);
        }

        @Test
        @DisplayName("will not acknowledge an unpublished report (blocked by visibility rule)")
        void cannotAcknowledgeDraft() {
            when(assignments.isParentOfStudent(PARENT_ID, STUDENT_ID)).thenReturn(true);
            when(reportDao.findReport(REPORT_ID)).thenReturn(Optional.of(report("DRAFT")));

            assertThatThrownBy(() -> service.acknowledge(user(PARENT_ID, "PARENT"), REPORT_ID))
                    .isInstanceOf(ValidationException.class);

            verify(reportDao, never()).acknowledge(REPORT_ID, PARENT_ID);
        }
    }
}
