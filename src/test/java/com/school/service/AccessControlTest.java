package com.school.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.school.dao.AssignmentDao;
import com.school.model.User;
import com.school.util.AccessDeniedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("AccessControl")
@ExtendWith(MockitoExtension.class)
class AccessControlTest {

    @Mock
    private AssignmentDao assignments;

    private AccessControl access;

    private static final long STUDENT_ID = 100L;

    private User user(long id, String role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        access = new AccessControl(assignments);
    }

    @Test
    @DisplayName("denies a null (unauthenticated) user")
    void deniesNullUser() {
        assertThat(access.canAccess(null, STUDENT_ID)).isFalse();
    }

    @Nested
    @DisplayName("ADMIN")
    class Admin {
        @Test
        @DisplayName("can access any student without consulting assignments")
        void adminSeesAll() {
            assertThat(access.canAccess(user(1, "ADMIN"), STUDENT_ID)).isTrue();
        }
    }

    @Nested
    @DisplayName("TEACHER")
    class Teacher {
        @Test
        @DisplayName("can access a student in their caseload")
        void teacherInCaseload() {
            when(assignments.isTeacherOfStudent(2L, STUDENT_ID)).thenReturn(true);
            assertThat(access.canAccess(user(2, "TEACHER"), STUDENT_ID)).isTrue();
        }

        @Test
        @DisplayName("cannot access a student outside their caseload")
        void teacherOutsideCaseload() {
            when(assignments.isTeacherOfStudent(2L, STUDENT_ID)).thenReturn(false);
            assertThat(access.canAccess(user(2, "TEACHER"), STUDENT_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("PARENT")
    class Parent {
        @Test
        @DisplayName("can access their own child")
        void parentOwnChild() {
            when(assignments.isParentOfStudent(3L, STUDENT_ID)).thenReturn(true);
            assertThat(access.canAccess(user(3, "PARENT"), STUDENT_ID)).isTrue();
        }

        @Test
        @DisplayName("cannot access a child that is not theirs")
        void parentOtherChild() {
            when(assignments.isParentOfStudent(3L, STUDENT_ID)).thenReturn(false);
            assertThat(access.canAccess(user(3, "PARENT"), STUDENT_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("require()")
    class Require {
        @Test
        @DisplayName("throws AccessDeniedException when not permitted")
        void throwsWhenDenied() {
            lenient().when(assignments.isParentOfStudent(3L, STUDENT_ID)).thenReturn(false);
            assertThatThrownBy(() -> access.require(user(3, "PARENT"), STUDENT_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("passes silently when permitted")
        void passesWhenAllowed() {
            assertThatNoException()
                    .isThrownBy(() -> access.require(user(1, "ADMIN"), STUDENT_ID));
        }
    }
}
