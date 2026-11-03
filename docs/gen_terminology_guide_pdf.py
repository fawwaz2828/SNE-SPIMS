# -*- coding: utf-8 -*-
"""Generates SNE-SPIMS-Terminology-Guide.pdf — a plain-English study guide of the
terms a presenter should know to demo the app and answer questions (tech stack,
architecture/backend, security, domain terms, and a Q&A cheat-sheet)."""
import os
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
                                PageBreak, Image, HRFlowable, KeepTogether)

HERE = os.path.dirname(os.path.abspath(__file__))
LOGO = os.path.join(HERE, "..", "src", "main", "resources", "static", "images", "lunabridge.png")
OUT = os.path.join(HERE, "SNE-SPIMS-Terminology-Guide.pdf")

RED      = colors.HexColor("#B23A2E")
RED_DARK = colors.HexColor("#7A2820")
RED_TINT = colors.HexColor("#FBEDEA")
INK      = colors.HexColor("#2B2320")
INK_SOFT = colors.HexColor("#6B5E58")
BORDER   = colors.HexColor("#E7DED9")
BG_SOFT  = colors.HexColor("#FAF6F4")

styles = getSampleStyleSheet()
def S(name, **kw):
    styles.byName[name] = ParagraphStyle(name, parent=styles["Normal"], **kw)

S("H1", fontName="Helvetica-Bold", fontSize=19, textColor=RED_DARK, spaceBefore=4, spaceAfter=9, leading=23)
S("H2", fontName="Helvetica-Bold", fontSize=13, textColor=RED, spaceBefore=12, spaceAfter=5, leading=17)
S("Body", fontName="Helvetica", fontSize=10, textColor=INK, leading=14.5, spaceAfter=5)
S("Muted", fontName="Helvetica", fontSize=9, textColor=INK_SOFT, leading=12.5)
S("Term", fontName="Helvetica-Bold", fontSize=10, textColor=INK, leading=13)
S("Def", fontName="Helvetica", fontSize=9.3, textColor=INK, leading=12.8)
S("App", fontName="Helvetica-Oblique", fontSize=9.3, textColor=RED_DARK, leading=12.8)
S("Cover", fontName="Helvetica-Bold", fontSize=28, textColor=RED_DARK, leading=32, alignment=TA_CENTER)
S("CoverSub", fontName="Helvetica", fontSize=13, textColor=INK_SOFT, leading=18, alignment=TA_CENTER)
S("Say", fontName="Helvetica-Oblique", fontSize=10, textColor=INK, leading=14.5, spaceAfter=4)
S("QQ", fontName="Helvetica-Bold", fontSize=10, textColor=INK, leading=13.5, spaceBefore=7)
S("QA", fontName="Helvetica", fontSize=9.6, textColor=INK, leading=13.2, spaceAfter=2)

story = []

def hr():
    story.append(Spacer(1, 3)); story.append(HRFlowable(width="100%", thickness=0.7, color=BORDER)); story.append(Spacer(1, 3))

def terms(rows):
    """rows: list of (term, definition, in-app usage). Renders a 2-col table."""
    data = []
    for name, definition, inapp in rows:
        left = Paragraph(name, styles["Term"])
        right = [Paragraph(definition, styles["Def"])]
        if inapp:
            right.append(Spacer(1, 2))
            right.append(Paragraph("In this app: " + inapp, styles["App"]))
        data.append([left, right])
    t = Table(data, colWidths=[42*mm, 118*mm])
    style = [
        ("VALIGN",(0,0),(-1,-1),"TOP"),
        ("LEFTPADDING",(0,0),(-1,-1),8),("RIGHTPADDING",(0,0),(-1,-1),8),
        ("TOPPADDING",(0,0),(-1,-1),7),("BOTTOMPADDING",(0,0),(-1,-1),7),
        ("LINEBELOW",(0,0),(-1,-1),0.5,BORDER),
        ("LINEAFTER",(0,0),(0,-1),0.5,BORDER),
        ("BOX",(0,0),(-1,-1),0.5,BORDER),
    ]
    for r in range(len(data)):
        if r % 2 == 1:
            style.append(("BACKGROUND",(0,r),(-1,r), BG_SOFT))
    t.setStyle(TableStyle(style))
    story.append(t)
    story.append(Spacer(1, 8))

def callout(title, text):
    inner = [Paragraph("<b>%s</b>" % title, styles["Def"]), Spacer(1,2), Paragraph(text, styles["Def"])]
    t = Table([[inner]], colWidths=[160*mm])
    t.setStyle(TableStyle([
        ("BACKGROUND",(0,0),(-1,-1), RED_TINT), ("BOX",(0,0),(-1,-1),0.5,RED),
        ("LEFTPADDING",(0,0),(-1,-1),10),("RIGHTPADDING",(0,0),(-1,-1),10),
        ("TOPPADDING",(0,0),(-1,-1),8),("BOTTOMPADDING",(0,0),(-1,-1),8),
    ]))
    story.append(t); story.append(Spacer(1, 8))

def qa(q, a):
    story.append(KeepTogether([Paragraph("Q: " + q, styles["QQ"]), Paragraph("A: " + a, styles["QA"])]))

# ---------------- COVER ----------------
story.append(Spacer(1, 46))
if os.path.exists(LOGO):
    try: story.append(Image(LOGO, width=64*mm, height=64*mm, kind="proportional"))
    except Exception: pass
story.append(Spacer(1, 14))
story.append(Paragraph("SNE-SPIMS", styles["Cover"]))
story.append(Paragraph("Terminology Study Guide", styles["CoverSub"]))
story.append(Spacer(1, 8))
story.append(Paragraph("Know every term you'll say (or be asked) in the demo &amp; tech talk", styles["CoverSub"]))
story.append(Spacer(1, 26))
story.append(Paragraph("Tech stack &bull; Architecture &bull; Security &bull; Domain terms &bull; Q&amp;A", styles["CoverSub"]))
story.append(PageBreak())

# ---------------- 0. ELEVATOR PITCH ----------------
story.append(Paragraph("0. Say this first (memorise it)", styles["H1"]))
story.append(Paragraph("The 20-second description of the whole project:", styles["Muted"]))
story.append(Spacer(1, 4))
callout("Elevator pitch",
    "“SNE-SPIMS is a web application for a special-needs school. Parents, teachers, and an admin "
    "share one system, but each role only sees what's relevant to them. It's built with Spring Boot "
    "and renders pages on the server with Thymeleaf; data lives in a MySQL database accessed through "
    "MyBatis. Every piece of information is tied to a child, and access is controlled by role.”")
story.append(Paragraph("What the letters mean", styles["H2"]))
terms([
    ("SNE", "Special-Needs Education — schooling adapted for students with disabilities.", None),
    ("SPIMS", "Student Progress &amp; Information Management System — the kind of system this is.", None),
    ("Luna Bridge", "The school's brand name shown in the UI (logo, login). Same product as SNE-SPIMS.", None),
])

# ---------------- 1. TECH STACK ----------------
story.append(Paragraph("1. Tech-stack terms", styles["H1"]))
terms([
    ("Java", "A widely-used, statically-typed programming language that runs on the JVM (Java Virtual Machine).",
     "The whole backend is Java; built to target Java 17, runs on a newer JDK."),
    ("Spring Boot", "The most popular Java framework for building web apps; auto-configures everything and bundles a web server so the app runs as a single program.",
     "Version 3.3.5 — the foundation of the app; starts an embedded Tomcat server on port 8080."),
    ("Spring MVC", "The part of Spring that handles web requests using the Model-View-Controller pattern.",
     "Maps URLs like /dashboard to Java methods (controllers) that return a page."),
    ("Spring Security", "Spring's library for login, passwords, and access control.",
     "Provides the login form, checks passwords, and enforces which role can open which URL."),
    ("Thymeleaf", "A server-side HTML template engine: HTML files with special th:* attributes that get filled with real data before being sent to the browser.",
     "Every page (dashboard, login, messages…) is a Thymeleaf template."),
    ("Thymeleaf Layout Dialect", "An add-on that lets all pages share one common layout (header, side rail) instead of repeating it.",
     "layout/main.html is the shared shell every page 'decorates'."),
    ("MyBatis", "A 'SQL mapper' — you write the SQL yourself in XML files and it maps the results to Java objects. (Lighter than a full ORM like JPA/Hibernate.)",
     "All database queries live in mappers/*.xml; used standalone, wired by hand."),
    ("MySQL", "A popular open-source relational database (data stored in tables with rows and columns).",
     "Stores users, students, IEPs, reports, sessions, messages, etc."),
    ("MariaDB / XAMPP", "MariaDB is a drop-in MySQL replacement; XAMPP is an easy bundle that installs MySQL/MariaDB + tools on a PC for local development.",
     "The dev database runs under XAMPP locally."),
    ("HikariCP", "A connection pool — it keeps a set of open database connections ready to reuse, instead of opening a new one each time (much faster).",
     "One shared pool; MyBatis borrows connections from it."),
    ("JDBC", "Java Database Connectivity — the standard Java API for talking to SQL databases; the driver is the database-specific plug-in.",
     "The MySQL JDBC driver is what actually sends SQL to the database."),
    ("BCrypt / jBCrypt", "A secure, slow hashing algorithm designed for passwords; jBCrypt is its Java implementation.",
     "Passwords are stored as BCrypt hashes ($2a$…), never as plain text."),
    ("Maven", "A build tool for Java — it downloads libraries and compiles/packages the app.",
     "Bundled Maven builds and runs the app (mvn spring-boot:run)."),
    ("Bootstrap", "A popular CSS framework of ready-made UI components (buttons, dropdowns, grids).",
     "Used for base components; restyled with the app's warm 'clay' colour theme."),
    ("Lexend", "A Google font designed to reduce reading fatigue — good for accessibility.",
     "The app's UI font."),
    ("oklch", "A modern way to define colours by lightness, chroma (saturation), and hue — easy to keep a palette consistent.",
     "All design colours/tokens in app.css are written in oklch."),
])

# ---------------- 2. ARCHITECTURE / BACKEND ----------------
story.append(Paragraph("2. Architecture &amp; backend terms", styles["H1"]))
terms([
    ("Layered architecture", "Organising code in layers, each with one job, calling only the layer below: Controller → Service → DAO → Database.",
     "Keeps web code, business rules, and SQL cleanly separated."),
    ("MVC (Model-View-Controller)", "A pattern: the Controller handles the request, the Model is the data, the View is the page shown.",
     "Controller (Java) + Model (data) + View (Thymeleaf HTML)."),
    ("Controller", "A class that receives a web request and decides what to do, then returns which page to show.",
     "e.g. DashboardController handles GET /dashboard."),
    ("Service layer", "Where the business rules live (the 'what should happen' logic), independent of the web or database.",
     "e.g. ReportService, AccessControl — also where access rules are enforced."),
    ("DAO (Data Access Object)", "A class whose only job is to read/write one type of data in the database.",
     "e.g. StudentDao — its SQL lives in StudentMapper.xml."),
    ("Model / POJO", "A 'Plain Old Java Object' — a simple class holding fields that mirror a database row.",
     "e.g. Student, DailyReport, IepGoal."),
    ("Dependency Injection (DI)", "Instead of a class creating the objects it needs, the framework hands them in. Makes code easier to test and swap.",
     "Spring injects services into controllers; objects called 'beans'."),
    ("Bean", "Any object that Spring creates and manages for you.",
     "Services are exposed as beans so controllers can use them."),
    ("Service locator / Singleton", "Singleton = exactly one shared instance of a class. A service locator is one object that hands out all the others.",
     "A hand-wired Services locator builds the DAOs/services once; a small config bridges them into Spring."),
    ("Connection pool", "A ready set of reusable database connections (see HikariCP).",
     "Avoids the cost of opening a new DB connection per request."),
    ("Server-side rendering (SSR)", "The server builds the full HTML page and sends it ready-to-display (vs. a JavaScript app that builds pages in the browser).",
     "Why the app feels simple and has no separate front-end build."),
    ("Template engine", "The tool that merges data into an HTML template (see Thymeleaf).", None),
    ("Application event", "An in-app message: one part 'publishes' an event, another 'listens' and reacts — keeps features decoupled.",
     "Publishing a daily report fires an event that creates a parent notification."),
])

# ---------------- 3. WEB & SECURITY ----------------
story.append(Paragraph("3. Web &amp; security terms", styles["H1"]))
terms([
    ("HTTP", "The protocol browsers use to talk to servers, via requests and responses.", None),
    ("GET vs POST", "GET asks for a page/data; POST submits data (a form) that changes something.",
     "GET /dashboard shows a page; POST /messages/send sends a chat message."),
    ("Endpoint / route", "A specific URL the server responds to.", "e.g. /students, /reports/new."),
    ("Session", "The server's memory that you're logged in, kept across requests via a cookie.",
     "Keeps you signed in as you move between pages."),
    ("Cookie", "A small piece of data the browser stores and sends back to the server (e.g. your session id).", None),
    ("Authentication vs Authorization", "Authentication = proving who you are (login). Authorization = what you're allowed to do.",
     "Login authenticates; role checks authorize."),
    ("Role-based access control (RBAC)", "Permissions are granted by role, not per-person.",
     "Three roles: ADMIN, TEACHER, PARENT — each sees different menus/data."),
    ("CSRF", "Cross-Site Request Forgery — an attack where another site tricks your browser into submitting a form; a hidden token blocks it.",
     "Every form includes a CSRF token; Spring Security checks it."),
    ("Form login", "Logging in with a username/password HTML form (vs. tokens/SSO).",
     "The /login page; passwords verified against BCrypt hashes."),
    ("CSS variables / design tokens", "Named, reusable values (colours, spacing) defined once and used everywhere.",
     "app.css defines --red, --surface, etc. — change once, applies app-wide."),
    ("Favicon", "The small icon shown on the browser tab.", "Set to the Luna Bridge logo."),
])

# ---------------- 4. DOMAIN ----------------
story.append(Paragraph("4. Domain terms (special-needs education)", styles["H1"]))
terms([
    ("IEP (Individualised Education Program)", "A formal, personalised plan describing a student's goals, current levels, and the accommodations they need.",
     "The long-term progress tracker; each student has one active IEP."),
    ("IEP goal", "A single target within an IEP, in a development area, with a progress percentage.",
     "Updated over time; the history drives the progress trend."),
    ("Development domain / area", "The five areas progress is grouped into: Academic, Communication, Social/Emotional, Self-Care, Motor.",
     "Each has its own colour in the UI."),
    ("Daily report", "A short note a teacher writes about a student's day (summary, mood, ratings).",
     "Published to parents, who can 'acknowledge' (mark as read)."),
    ("Rubric", "A set of criteria with rating levels used to assess consistently (e.g. Emerging → Developing → Proficient).",
     "Each daily report scores a few rubric criteria."),
    ("Therapy session", "A scheduled specialist session (speech, occupational, physical, counselling) in a room.",
     "Scheduling runs a 3-way clash check: student, therapist, and room."),
    ("Behaviour incident", "A recorded event of concerning behaviour, with a severity and the action taken.",
     "Severity LOW / MEDIUM / HIGH; a 'parent-visible' switch controls if families see it."),
    ("Caseload / roster", "Caseload = the students a teacher is responsible for. Roster = the full list of students.",
     "A teacher only sees their caseload; admin sees the whole roster."),
    ("Announcement", "A school notice targeted at everyone, teachers, or parents.",
     "Created by admin; shown as a dashboard widget."),
    ("Notification", "A personal alert (bell icon) — new report, therapy reminder, new message.",
     "Clicking one deep-links to the exact page."),
    ("Acknowledge", "A parent confirming they've read a report.",
     "Marks the report read and notifies the teacher."),
])

# ---------------- 5. Q&A ----------------
story.append(Paragraph("5. Likely questions — short answers", styles["H1"]))
story.append(Paragraph("Read these once; they cover the questions an audience usually asks.", styles["Muted"]))
qa("Why MyBatis instead of JPA/Hibernate?",
   "We wanted full control over the SQL and a lighter data layer. MyBatis lets us write and tune the exact queries, with results mapped to Java objects — simpler to reason about for this project.")
qa("Why server-side rendering instead of React/Angular?",
   "One codebase, no separate front-end build, and security is enforced in one place on the server. Thymeleaf was the pragmatic choice for a content-driven school app.")
qa("How are passwords kept safe?",
   "They're never stored in plain text — only as BCrypt hashes, a slow algorithm built for passwords. Login re-hashes the input and compares.")
qa("How do you stop one user seeing another's data?",
   "Access is enforced in the service layer by a single AccessControl check: admins see everything, teachers only their caseload, parents only their own children. The UI also hides what you can't access, but the server is the real gate.")
qa("What happens if two therapy sessions overlap?",
   "Scheduling runs a three-way conflict check — the same student, therapist, or room can't be double-booked at the same time; the save is rejected with a clear message.")
qa("Is it scalable / production-ready?",
   "The architecture is clean and layered, with a connection pool and stateless request handling, so it can scale. For production we'd externalise the database credentials, add HTTPS, and run behind a proper server — it's currently tuned for a local demo.")
qa("Which database, and can it change?",
   "MySQL (MariaDB under XAMPP in development). Because all SQL is isolated in MyBatis mapper files and we use standard JDBC, switching databases mostly means swapping the driver and adjusting SQL.")
qa("What are the main features?",
   "Role dashboards, IEP goals &amp; progress, daily reports with parent acknowledgement, therapy scheduling with conflict checks, behaviour incidents, parent–teacher chat, a shared calendar, announcements, and notifications.")
qa("How is the look kept consistent?",
   "A design system: colours/spacing are CSS 'tokens' in one file (app.css) and every page shares one layout shell, so a single change updates the whole app.")
qa("What would you improve with more time?",
   "Automated tests coverage, real image assets, HTTPS, externalised secrets, and possibly a richer reporting/analytics view.")

# ---------------- BUILD ----------------
def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(BORDER); canvas.setLineWidth(0.5)
    canvas.line(20*mm, 14*mm, 190*mm, 14*mm)
    canvas.setFont("Helvetica", 8); canvas.setFillColor(INK_SOFT)
    canvas.drawString(20*mm, 9*mm, "SNE-SPIMS · Terminology Study Guide")
    canvas.drawRightString(190*mm, 9*mm, "Page %d" % doc.page)
    canvas.restoreState()

doc = SimpleDocTemplate(OUT, pagesize=A4, leftMargin=20*mm, rightMargin=20*mm,
                        topMargin=18*mm, bottomMargin=20*mm,
                        title="SNE-SPIMS Terminology Study Guide", author="SNE-SPIMS")
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print("WROTE", OUT)
