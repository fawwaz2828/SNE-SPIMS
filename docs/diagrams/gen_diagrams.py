"""Generate UML/diagram PNGs for the SNE-SPIMS design document."""
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Ellipse, FancyArrowPatch, Circle, Rectangle

OUT = os.path.dirname(os.path.abspath(__file__))

# Palette
INK = "#1f2933"
BLUE = "#2e6fb0"
BLUE_BG = "#dceaf6"
GREEN = "#2f8f5b"
GREEN_BG = "#d8efe2"
AMBER = "#b9770e"
AMBER_BG = "#fcefd2"
VIOLET = "#6a4ca8"
VIOLET_BG = "#e7ddf6"
GREY_BG = "#eef1f4"
CYAN_BG = "#d6f0f5"


def new_ax(w, h, xlim, ylim):
    fig, ax = plt.subplots(figsize=(w, h), dpi=150)
    ax.set_xlim(*xlim)
    ax.set_ylim(*ylim)
    ax.set_aspect("equal")
    ax.axis("off")
    return fig, ax


def box(ax, x, y, w, h, text, fc=BLUE_BG, ec=BLUE, fs=10, bold=False, round=True):
    style = "round,pad=0.02,rounding_size=0.12" if round else "square,pad=0.02"
    p = FancyBboxPatch((x, y), w, h, boxstyle=style, fc=fc, ec=ec, lw=1.4)
    ax.add_patch(p)
    ax.text(x + w / 2, y + h / 2, text, ha="center", va="center",
            fontsize=fs, color=INK, weight="bold" if bold else "normal", wrap=True)


def ellipse(ax, x, y, w, h, text, fc=GREEN_BG, ec=GREEN, fs=9):
    e = Ellipse((x, y), w, h, fc=fc, ec=ec, lw=1.3)
    ax.add_patch(e)
    ax.text(x, y, text, ha="center", va="center", fontsize=fs, color=INK)


def arrow(ax, x1, y1, x2, y2, color=INK, ls="-", lw=1.3, head=True, dashed=False):
    style = "-|>" if head else "-"
    fa = FancyArrowPatch((x1, y1), (x2, y2), arrowstyle=style, mutation_scale=12,
                         color=color, lw=lw, linestyle="--" if dashed else ls,
                         shrinkA=2, shrinkB=2)
    ax.add_patch(fa)


def actor(ax, x, y, label):
    ax.add_patch(Circle((x, y + 0.55), 0.18, fc="white", ec=INK, lw=1.4))      # head
    ax.plot([x, x], [y + 0.37, y - 0.15], color=INK, lw=1.4)                    # body
    ax.plot([x - 0.28, x + 0.28], [y + 0.20, y + 0.20], color=INK, lw=1.4)     # arms
    ax.plot([x, x - 0.22], [y - 0.15, y - 0.5], color=INK, lw=1.4)             # leg
    ax.plot([x, x + 0.22], [y - 0.15, y - 0.5], color=INK, lw=1.4)             # leg
    ax.text(x, y - 0.72, label, ha="center", va="center", fontsize=10, weight="bold", color=INK)


# ---------------------------------------------------------------------------
# 1. USE CASE DIAGRAM
# ---------------------------------------------------------------------------
def usecase():
    fig, ax = new_ax(11, 8.2, (0, 14), (0, 11))
    ax.text(7, 10.5, "SNE-SPIMS — Use Case Diagram", ha="center", fontsize=14, weight="bold", color=INK)

    # System boundary
    ax.add_patch(Rectangle((3.2, 0.5), 7.6, 9.3, fill=False, ec=INK, lw=1.6))
    ax.text(7, 9.5, "SNE-SPIMS", ha="center", fontsize=11, style="italic", color=INK)

    # Actors
    actor(ax, 1.3, 7.6, "Teacher")
    actor(ax, 1.3, 3.4, "Parent")
    actor(ax, 12.7, 6.2, "Admin")

    # Use cases (x, y, label)
    uc_teacher = [
        (5.0, 9.0, "Author / publish\ndaily report"),
        (5.0, 8.0, "Log behaviour\nincident"),
        (5.0, 7.0, "Manage IEP\ngoals & progress"),
        (5.0, 6.0, "Schedule therapy\nsession"),
    ]
    uc_shared = [
        (7.0, 5.0, "View role-scoped\ndashboard"),
        (7.0, 4.0, "Message\nparent / teacher"),
        (7.0, 3.0, "View unified\ncalendar"),
        (7.0, 2.0, "Read / archive\nnotifications"),
    ]
    uc_parent = [
        (5.0, 1.2, "Acknowledge\npublished report"),
    ]
    uc_admin = [
        (9.0, 9.0, "Manage user\naccounts"),
        (9.0, 8.0, "Publish\nannouncements"),
        (9.0, 7.0, "Assign teachers\n& parents"),
        (9.0, 6.0, "View school-wide\nanalytics"),
    ]
    for x, y, t in uc_teacher + uc_shared + uc_parent + uc_admin:
        ellipse(ax, x, y, 1.9, 0.78, t, fc=BLUE_BG, ec=BLUE)

    # associations
    for x, y, _ in uc_teacher + uc_shared:
        arrow(ax, 1.7, 7.4, x - 0.95, y, head=False, lw=1.0)
    for x, y, _ in uc_shared + uc_parent:
        arrow(ax, 1.7, 3.4, x - 0.95, y, head=False, lw=1.0)
    for x, y, _ in uc_admin + uc_shared:
        arrow(ax, 12.3, 6.2, x + 0.95, y, head=False, lw=1.0)

    fig.savefig(os.path.join(OUT, "uml_usecase.png"), bbox_inches="tight", facecolor="white")
    plt.close(fig)


# ---------------------------------------------------------------------------
# 2. LAYERED ARCHITECTURE / COMPONENT DIAGRAM
# ---------------------------------------------------------------------------
def architecture():
    fig, ax = new_ax(9.5, 11, (0, 12), (0, 15))
    ax.text(6, 14.4, "SNE-SPIMS — Layered Architecture", ha="center", fontsize=14, weight="bold", color=INK)

    layers = [
        (12.2, "Client (Browser)", ["Thymeleaf-rendered HTML", "Bootstrap 5 · Chart.js · bell poll (JS)"], GREY_BG, INK),
        (10.4, "Presentation — Spring MVC Controllers", [
            "Dashboard · Student · Report · Incident · IEP",
            "Therapy · Message · Calendar · Notification · User · Announcement"], BLUE_BG, BLUE),
        (8.6, "Security (Spring Security filter chain)", [
            "Form login · BCrypt · ROLE_* authorities · CSRF"], VIOLET_BG, VIOLET),
        (6.8, "Service / Business Layer", [
            "AccessControl · StudentService · ReportService · IepService",
            "IncidentService · TherapyService · MessageService · NotificationService"], GREEN_BG, GREEN),
        (5.0, "Persistence — DAO interfaces + Impl", [
            "StudentDao · DailyReportDao · IepDao · MessageDao · ..."], AMBER_BG, AMBER),
        (3.2, "MyBatis helper (com.school.util.MyBatis)", [
            "Mapper XML · ThreadLocal tx() · HikariCP pool"], CYAN_BG, BLUE),
        (1.4, "MySQL 8 Database (sne_spims)", [
            "16 core tables · FK constraints · indexes"], GREY_BG, INK),
    ]
    for y, title, lines, fc, ec in layers:
        box(ax, 1.0, y, 10.0, 1.5, "", fc=fc, ec=ec)
        ax.text(6, y + 1.12, title, ha="center", fontsize=11, weight="bold", color=INK)
        for i, ln in enumerate(lines):
            ax.text(6, y + 0.62 - i * 0.36, ln, ha="center", fontsize=8.5, color=INK)

    for y in [12.2, 10.4, 8.6, 6.8, 5.0, 3.2]:
        arrow(ax, 6, y, 6, y - 0.32, lw=1.6)
        arrow(ax, 5.5, y - 0.32, 5.5, y, lw=1.0, color="#888")

    ax.text(11.4, 9.35, "events", rotation=90, fontsize=8, color=VIOLET, va="center")
    fig.savefig(os.path.join(OUT, "uml_architecture.png"), bbox_inches="tight", facecolor="white")
    plt.close(fig)


# ---------------------------------------------------------------------------
# 3. DOMAIN CLASS DIAGRAM
# ---------------------------------------------------------------------------
def class_diagram():
    fig, ax = new_ax(12, 9, (0, 16), (0, 12))
    ax.text(8, 11.5, "SNE-SPIMS — Domain Class Diagram (core entities)", ha="center", fontsize=14, weight="bold", color=INK)

    def cls(x, y, w, name, attrs, fc=BLUE_BG, ec=BLUE):
        h_title = 0.5
        h = h_title + 0.32 * len(attrs) + 0.2
        box(ax, x, y - h, w, h, "", fc="white", ec=ec, round=False)
        box(ax, x, y - h_title, w, h_title, "", fc=fc, ec=ec, round=False)
        ax.text(x + w / 2, y - h_title / 2, name, ha="center", va="center", fontsize=9.5, weight="bold", color=INK)
        for i, a in enumerate(attrs):
            ax.text(x + 0.1, y - h_title - 0.26 - i * 0.32, a, ha="left", va="center", fontsize=7.6, color=INK)
        return (x, y, w, h)

    user = cls(0.4, 11.0, 3.2, "User", ["id, username", "fullName, email", "role: ADMIN/TEACHER/PARENT", "active"])
    student = cls(6.4, 11.0, 3.4, "Student", ["id, studentCode", "fullName, dateOfBirth", "disabilityType", "supportLevel, classGroup"])
    ts = cls(3.7, 7.5, 2.6, "TeacherStudent", ["teacherId", "studentId", "isPrimary"], fc=GREY_BG, ec=INK)
    ps = cls(0.4, 7.5, 2.6, "ParentStudent", ["parentId", "studentId", "relationship"], fc=GREY_BG, ec=INK)
    iep = cls(12.4, 11.0, 3.4, "IepProgram", ["id, title", "academicYear", "start/endDate", "status"], fc=GREEN_BG, ec=GREEN)
    goal = cls(12.4, 7.6, 3.4, "IepGoal", ["id, domain", "description", "status, progressPct"], fc=GREEN_BG, ec=GREEN)
    gph = cls(12.4, 4.3, 3.4, "GoalProgressHistory", ["goalId", "recordedOn", "progressPct"], fc=GREEN_BG, ec=GREEN)
    report = cls(6.4, 7.5, 3.4, "DailyReport", ["id, reportDate", "summary, mood", "status: DRAFT/PUBLISHED"], fc=AMBER_BG, ec=AMBER)
    rubric = cls(6.4, 4.6, 3.4, "ReportRubric", ["criterion", "rating", "comment"], fc=AMBER_BG, ec=AMBER)
    ack = cls(6.4, 2.0, 3.4, "ReportAcknowledgement", ["reportId", "parentId", "acknowledgedAt"], fc=AMBER_BG, ec=AMBER)
    incident = cls(0.4, 4.6, 3.4, "BehaviorIncident", ["id, incidentDate", "severity, description", "parentVisible"], fc=VIOLET_BG, ec=VIOLET)
    therapy = cls(0.4, 1.9, 3.4, "TherapySession", ["id, therapyType", "start/endTime", "status"], fc=CYAN_BG, ec=BLUE)
    msg = cls(3.7, 4.6, 2.6, "Message", ["sender/recipient", "studentId", "isRead"], fc=GREY_BG, ec=INK)

    def link(a, b, label="", dashed=False):
        ax_, ay_, aw_, ah_ = a
        bx_, by_, bw_, bh_ = b
        x1, y1 = ax_ + aw_ / 2, ay_ - ah_ / 2
        x2, y2 = bx_ + bw_ / 2, by_ - bh_ / 2
        arrow(ax, x1, y1, x2, y2, head=False, lw=1.0, dashed=dashed, color="#555")

    link(user, ts); link(student, ts); link(user, ps); link(student, ps)
    link(student, iep); link(iep, goal); link(goal, gph)
    link(student, report); link(report, rubric); link(report, ack)
    link(student, incident); link(student, therapy); link(student, msg)

    fig.savefig(os.path.join(OUT, "uml_class.png"), bbox_inches="tight", facecolor="white")
    plt.close(fig)


# ---------------------------------------------------------------------------
# 4. SEQUENCE DIAGRAM — create & publish daily report
# ---------------------------------------------------------------------------
def sequence():
    fig, ax = new_ax(12, 9, (0, 16), (0, 12))
    ax.text(8, 11.5, "Sequence — Create & Publish a Daily Report (with transaction + notification)",
            ha="center", fontsize=12.5, weight="bold", color=INK)

    actors = [
        (1.4, "Teacher"),
        (4.0, "ReportController"),
        (7.0, "ReportService"),
        (9.8, "MyBatis.tx"),
        (12.2, "DailyReportDao"),
        (14.6, "Notification\nService"),
    ]
    for x, name in actors:
        box(ax, x - 1.1, 10.0, 2.2, 0.7, name, fc=BLUE_BG, ec=BLUE, fs=8.5, bold=True)
        ax.plot([x, x], [10.0, 0.6], color="#999", lw=1.0, ls="--")

    def msg(x1, x2, y, text, ret=False):
        arrow(ax, x1, y, x2, y, lw=1.2, dashed=ret, color=INK)
        midx = (x1 + x2) / 2
        ax.text(midx, y + 0.16, text, ha="center", fontsize=7.8, color=INK)

    X = {n.split()[0]: x for x, n in [(1.4, "Teacher"), (4.0, "ReportController"),
         (7.0, "ReportService"), (9.8, "MyBatis.tx"), (12.2, "DailyReportDao"), (14.6, "Notification")]}

    msg(X["Teacher"], X["ReportController"], 9.4, "POST /students/{id}/reports")
    msg(X["ReportController"], X["ReportService"], 9.0, "createDraft(user, report, ratings)")
    ax.text(X["ReportService"], 8.6, "requireAuthor() + validateRubrics()", ha="center", fontsize=7.5, style="italic", color=GREEN)
    msg(X["ReportService"], X["MyBatis.tx"], 8.2, "tx(() -> { ... })")
    msg(X["MyBatis.tx"], X["DailyReportDao"], 7.8, "insertReport()")
    msg(X["MyBatis.tx"], X["DailyReportDao"], 7.4, "insertRubric() × N")
    ax.text(X["MyBatis.tx"], 7.0, "commit (or rollback on error)", ha="center", fontsize=7.5, style="italic", color=AMBER)
    msg(X["ReportService"], X["ReportController"], 6.6, "reportId", ret=True)

    ax.plot([0.6, 15.4], [6.2, 6.2], color="#ccc", lw=0.8)
    ax.text(8, 6.0, "— later: teacher publishes —", ha="center", fontsize=8, color="#777")

    msg(X["Teacher"], X["ReportController"], 5.6, "POST /reports/{id}/publish")
    msg(X["ReportController"], X["ReportService"], 5.2, "getWithRubrics() [access check]")
    msg(X["ReportController"], X["ReportService"], 4.8, "publish(user, id)")
    msg(X["ReportController"], X["Notification"], 4.4, "publishEvent(DailyReportPublished)")
    ax.text(X["Notification"], 4.0, "async listener", ha="center", fontsize=7.3, style="italic", color=VIOLET)
    msg(X["Notification"], X["MyBatis.tx"], 3.6, "tx → notify each parent")
    msg(X["ReportController"], X["Teacher"], 3.1, "redirect → report page", ret=True)

    fig.savefig(os.path.join(OUT, "uml_sequence.png"), bbox_inches="tight", facecolor="white")
    plt.close(fig)


# ---------------------------------------------------------------------------
# 5. ACCESS CONTROL ACTIVITY DIAGRAM
# ---------------------------------------------------------------------------
def access_activity():
    fig, ax = new_ax(7.5, 10, (0, 10), (0, 14))
    ax.text(5, 13.4, "Activity — AccessControl.canAccess(user, studentId)", ha="center", fontsize=12.5, weight="bold", color=INK)

    ax.add_patch(Circle((5, 12.6), 0.18, fc=INK, ec=INK))
    box(ax, 3.2, 11.3, 3.6, 0.8, "user == null ?", fc=AMBER_BG, ec=AMBER, bold=True)
    arrow(ax, 5, 12.42, 5, 12.1)

    box(ax, 0.3, 11.3, 2.2, 0.8, "return false", fc="#f6d6d6", ec="#b03a3a")
    arrow(ax, 3.2, 11.7, 2.5, 11.7); ax.text(2.85, 11.9, "yes", fontsize=8, color=INK)

    box(ax, 3.2, 9.9, 3.6, 0.8, "switch (user.role)", fc=BLUE_BG, ec=BLUE, bold=True)
    arrow(ax, 5, 11.3, 5, 10.7); ax.text(5.25, 11.0, "no", fontsize=8, color=INK)

    box(ax, 0.6, 8.2, 2.4, 0.9, "ADMIN\n→ true", fc=GREEN_BG, ec=GREEN)
    box(ax, 3.6, 8.2, 2.8, 0.9, "TEACHER\n→ isTeacherOf\nStudent()", fc=GREEN_BG, ec=GREEN)
    box(ax, 7.0, 8.2, 2.6, 0.9, "PARENT\n→ isParentOf\nStudent()", fc=GREEN_BG, ec=GREEN)
    arrow(ax, 4.4, 9.9, 1.8, 9.1)
    arrow(ax, 5.0, 9.9, 5.0, 9.1)
    arrow(ax, 5.6, 9.9, 8.3, 9.1)

    box(ax, 3.2, 6.4, 3.6, 0.8, "default → false", fc="#f6d6d6", ec="#b03a3a")
    arrow(ax, 5.0, 8.2, 5.0, 7.2, dashed=True, color="#999")

    box(ax, 3.2, 4.8, 3.6, 0.9, "require(): throw\nAccessDeniedException\nif not allowed", fc=VIOLET_BG, ec=VIOLET, bold=True)
    arrow(ax, 1.8, 8.2, 3.4, 5.7, head=False, color="#777", lw=0.9)
    arrow(ax, 5.0, 8.2, 5.0, 5.7, head=False, color="#777", lw=0.9)
    arrow(ax, 8.3, 8.2, 6.6, 5.7, head=False, color="#777", lw=0.9)

    ax.add_patch(Circle((5, 4.0), 0.22, fc="white", ec=INK, lw=1.6))
    ax.add_patch(Circle((5, 4.0), 0.12, fc=INK, ec=INK))
    arrow(ax, 5, 4.8, 5, 4.22)

    fig.savefig(os.path.join(OUT, "uml_activity.png"), bbox_inches="tight", facecolor="white")
    plt.close(fig)


def state_report():
    fig, ax = new_ax(10, 4.4, (0, 16), (0, 7))
    ax.text(8, 6.5, "State Machine — Daily Report Lifecycle", ha="center", fontsize=13, weight="bold", color=INK)

    ax.add_patch(Circle((1.2, 3.5), 0.22, fc=INK, ec=INK))
    box(ax, 3.0, 2.9, 3.0, 1.2, "DRAFT", fc=AMBER_BG, ec=AMBER, fs=12, bold=True)
    box(ax, 8.5, 2.9, 3.0, 1.2, "PUBLISHED", fc=GREEN_BG, ec=GREEN, fs=12, bold=True)
    box(ax, 12.6, 0.7, 3.0, 1.1, "ACKNOWLEDGED\n(by parent)", fc=BLUE_BG, ec=BLUE, fs=9.5, bold=True)

    arrow(ax, 1.42, 3.5, 3.0, 3.5)
    ax.text(2.2, 3.7, "create", fontsize=8.5, color=INK)

    arrow(ax, 6.0, 3.7, 8.5, 3.7)
    ax.text(7.25, 3.95, "publish()", fontsize=9, color=GREEN)
    arrow(ax, 8.5, 3.3, 6.0, 3.3)
    ax.text(7.25, 2.95, "unpublish()", fontsize=9, color=AMBER)

    ax.text(4.5, 2.55, "edit (while DRAFT)", fontsize=8, color="#777", ha="center")
    fa = FancyArrowPatch((3.6, 4.1), (5.4, 4.1), connectionstyle="arc3,rad=-0.9",
                         arrowstyle="-|>", mutation_scale=11, color="#777", lw=1.1)
    ax.add_patch(fa)

    arrow(ax, 10.5, 2.9, 13.2, 1.8)
    ax.text(12.4, 2.5, "acknowledge()", fontsize=8.5, color=BLUE)
    ax.text(10.2, 4.35, "parents notified", fontsize=8, style="italic", color=VIOLET, ha="center")

    fig.savefig(os.path.join(OUT, "uml_state.png"), bbox_inches="tight", facecolor="white")
    plt.close(fig)


usecase()
architecture()
class_diagram()
sequence()
access_activity()
state_report()
print("Diagrams written to", OUT)
