# -*- coding: utf-8 -*-
"""Generates SNE-SPIMS-Demo-Script.pdf — a feature-by-feature DEMO script
(what to click = 'Do', what to say = 'Say'), ending with only the most
important terms to study."""
import os
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
                                PageBreak, Image, HRFlowable, ListFlowable, ListItem, KeepTogether)

HERE = os.path.dirname(os.path.abspath(__file__))
LOGO = os.path.join(HERE, "..", "src", "main", "resources", "static", "images", "lunabridge.png")
OUT = os.path.join(HERE, "SNE-SPIMS-Demo-Script.pdf")

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

S("H1", fontName="Helvetica-Bold", fontSize=18, textColor=RED_DARK, spaceBefore=4, spaceAfter=8, leading=22)
S("Part", fontName="Helvetica-Bold", fontSize=12.5, textColor=colors.white, leading=16)
S("Feat", fontName="Helvetica-Bold", fontSize=12, textColor=RED_DARK, spaceBefore=12, spaceAfter=4, leading=15)
S("Body", fontName="Helvetica", fontSize=10, textColor=INK, leading=14.5, spaceAfter=5)
S("Muted", fontName="Helvetica", fontSize=9, textColor=INK_SOFT, leading=12.5)
S("DoLbl", fontName="Helvetica-Bold", fontSize=9, textColor=RED, leading=12)
S("Do", fontName="Helvetica", fontSize=9.4, textColor=INK, leading=12.8)
S("Say", fontName="Helvetica-Oblique", fontSize=10, textColor=INK, leading=14.5, spaceAfter=3)
S("Cover", fontName="Helvetica-Bold", fontSize=28, textColor=RED_DARK, leading=32, alignment=TA_CENTER)
S("CoverSub", fontName="Helvetica", fontSize=13, textColor=INK_SOFT, leading=18, alignment=TA_CENTER)
S("Term", fontName="Helvetica-Bold", fontSize=10, textColor=INK, leading=13)
S("Def", fontName="Helvetica", fontSize=9.3, textColor=INK, leading=12.8)
S("App", fontName="Helvetica-Oblique", fontSize=9.3, textColor=RED_DARK, leading=12.8)

story = []

def part(label):
    p = Paragraph(label, styles["Part"])
    t = Table([[p]], colWidths=[170*mm])
    t.setStyle(TableStyle([("BACKGROUND",(0,0),(-1,-1), RED),
        ("LEFTPADDING",(0,0),(-1,-1),12),("RIGHTPADDING",(0,0),(-1,-1),12),
        ("TOPPADDING",(0,0),(-1,-1),7),("BOTTOMPADDING",(0,0),(-1,-1),7)]))
    story.append(Spacer(1, 6)); story.append(t); story.append(Spacer(1, 4))

def feature(title, do_steps, say_lines):
    flow = [Paragraph(title, styles["Feat"])]
    flow.append(Paragraph("DO", styles["DoLbl"]))
    items = [ListItem(Paragraph(s, styles["Do"]), leftIndent=8) for s in do_steps]
    flow.append(ListFlowable(items, bulletType="1", leftIndent=14, bulletFontSize=8.5, bulletColor=RED, spaceAfter=4))
    flow.append(Spacer(1, 2))
    flow.append(Paragraph("SAY", styles["DoLbl"]))
    for s in say_lines:
        flow.append(Paragraph("&ldquo;" + s + "&rdquo;", styles["Say"]))
    story.append(KeepTogether(flow))
    story.append(Spacer(1, 4))

def callout(title, text):
    inner = [Paragraph("<b>%s</b>" % title, styles["Def"]), Spacer(1,2), Paragraph(text, styles["Def"])]
    t = Table([[inner]], colWidths=[170*mm])
    t.setStyle(TableStyle([("BACKGROUND",(0,0),(-1,-1), RED_TINT), ("BOX",(0,0),(-1,-1),0.5,RED),
        ("LEFTPADDING",(0,0),(-1,-1),10),("RIGHTPADDING",(0,0),(-1,-1),10),
        ("TOPPADDING",(0,0),(-1,-1),8),("BOTTOMPADDING",(0,0),(-1,-1),8)]))
    story.append(t); story.append(Spacer(1, 8))

def terms(rows):
    data = []
    for name, definition, say in rows:
        right = [Paragraph(definition, styles["Def"]), Spacer(1, 2),
                 Paragraph("If asked, say: " + say, styles["App"])]
        data.append([Paragraph(name, styles["Term"]), right])
    t = Table(data, colWidths=[40*mm, 130*mm])
    style = [("VALIGN",(0,0),(-1,-1),"TOP"),
        ("LEFTPADDING",(0,0),(-1,-1),8),("RIGHTPADDING",(0,0),(-1,-1),8),
        ("TOPPADDING",(0,0),(-1,-1),7),("BOTTOMPADDING",(0,0),(-1,-1),7),
        ("LINEBELOW",(0,0),(-1,-1),0.5,BORDER),("LINEAFTER",(0,0),(0,-1),0.5,BORDER),
        ("BOX",(0,0),(-1,-1),0.5,BORDER)]
    for r in range(len(data)):
        if r % 2 == 1: style.append(("BACKGROUND",(0,r),(-1,r), BG_SOFT))
    t.setStyle(TableStyle(style)); story.append(t); story.append(Spacer(1, 8))

# ---------------- COVER ----------------
story.append(Spacer(1, 46))
if os.path.exists(LOGO):
    try: story.append(Image(LOGO, width=64*mm, height=64*mm, kind="proportional"))
    except Exception: pass
story.append(Spacer(1, 14))
story.append(Paragraph("SNE-SPIMS", styles["Cover"]))
story.append(Paragraph("Live-Demo Script", styles["CoverSub"]))
story.append(Spacer(1, 8))
story.append(Paragraph("Feature by feature: what to click, what to say", styles["CoverSub"]))
story.append(Spacer(1, 26))
story.append(Paragraph("+ the few terms that actually matter, at the end", styles["CoverSub"]))
story.append(PageBreak())

# ---------------- SETUP ----------------
story.append(Paragraph("Before you start", styles["H1"]))
story.append(Paragraph("A 60-second checklist so nothing breaks mid-demo:", styles["Muted"]))
story.append(Spacer(1, 4))
items = [
    "Database is running (XAMPP / MySQL), and the app is started — open <b>http://localhost:8080</b>.",
    "Have the login accounts ready: <b>admin / admin123</b>, <b>james / teacher123</b>, <b>linda / parent123</b>.",
    "Tip: open a second browser window (or an incognito tab) so you can stay logged in as a teacher in one and a parent in the other — switching roles instantly looks slick.",
    "Zoom the browser to ~110% so the audience can read it.",
]
story.append(ListFlowable([ListItem(Paragraph(s, styles["Do"]), leftIndent=8) for s in items],
                          bulletType="bullet", start="•", bulletColor=RED, leftIndent=14, spaceAfter=4))
callout("Opening line (memorise)",
    "“SNE-SPIMS is one web app for a special-needs school. Parents, teachers, and an admin share it, "
    "but each role sees only what's relevant. Let me show you the same login take three different people "
    "to three different worlds.”")

# ---------------- PART 1: LOGIN ----------------
part("PART 1 — One login, three roles")
feature("1. Sign in", [
    "Show the login page (point out the logo and the calm design).",
    "Type <b>admin / admin123</b> and sign in.",
], [
    "Everyone enters through this same page. The system checks the password, recognises the role, and "
    "sends you to the dashboard built for you.",
    "Watch the left icon rail — the menu changes per role. I'm in as the administrator now.",
])

# ---------------- PART 2: ADMIN ----------------
part("PART 2 — Admin: the whole-school view")
feature("2. Admin dashboard", [
    "Point at the hero banner (incident summary), then the coloured Overview cards.",
    "Scroll through 'Students by disability', 'This week's therapy', 'Recent incidents'.",
    "Point at the right side: mini-calendar, upcoming events, announcements.",
], [
    "The admin sees statistics, not a to-do list: total students, active IEPs, recent incidents, staff "
    "counts, this week's therapy.",
    "Everything here is real data from the database — the disability breakdown, the sessions, the "
    "incidents with their severity. Nothing is hard-coded.",
])
feature("3. Students &amp; users", [
    "Click <b>Students</b> in the rail — show the roster, open one student profile.",
    "Click <b>Users</b> — show accounts; mention you can add/disable users and reset passwords.",
    "Click <b>Announcements</b> — show a notice and that admins target it to all / teachers / parents.",
], [
    "The admin manages the whole school: the student roster, the staff and parent accounts, and "
    "school-wide announcements.",
    "Notice the admin has no Messages — by design, private parent–teacher chat stays between them.",
])

# ---------------- PART 3: TEACHER ----------------
part("PART 3 — Teacher: a working day")
feature("4. Teacher dashboard (a worklist)", [
    "Sign out, log in as <b>james / teacher123</b>.",
    "Point at 'Reports to write' and the stat cards.",
], [
    "A teacher gets an actionable worklist: how many students, unread messages, this week's sessions, and "
    "— most useful — which students still need a daily report.",
])
feature("5. Write &amp; publish a daily report", [
    "From 'Reports to write', click <b>Write report</b> for a student.",
    "Fill a short summary, pick a mood, set a couple of rubric ratings.",
    "Click <b>Publish</b>.",
], [
    "Writing a report takes a summary, a mood, and a few rubric scores.",
    "The moment I publish, an event fires and the child's parent is notified automatically — I'll show "
    "that from the parent's side in a minute.",
])
feature("6. IEP — the long-term progress tracker", [
    "Open a student → <b>IEP</b> tab.",
    "Show the goals grouped by development area, each with a progress percentage.",
], [
    "The IEP is the individualised education plan — the long-term tracker. Each goal sits in a development "
    "area and carries a progress percentage that we update over time, which feeds the progress trend.",
])
feature("7. Therapy scheduling — the conflict check", [
    "Open <b>Therapy</b> → schedule a new session.",
    "Deliberately pick a room + time that's already taken, then submit.",
    "Show the rejection message; fix it and save successfully.",
], [
    "When we schedule therapy, the system runs a three-way conflict check: the same student, therapist, or "
    "room can't be double-booked. Watch — it refuses the clash, then accepts a clean slot.",
])
feature("8. Behaviour incident", [
    "Click <b>Incidents</b> → log a new one.",
    "Set a severity (LOW / MEDIUM / HIGH) and toggle <b>parent-visible</b>.",
], [
    "Incidents record what happened, the severity, and the action taken. This 'parent-visible' switch "
    "decides whether the family sees it — sensitive cases can stay staff-only.",
])
feature("9. Parent–teacher chat", [
    "Click <b>Messages</b> — open a parent's chat room and send a line.",
], [
    "Messaging is a chat room, not email. The teacher already has a room per parent–child, and only the "
    "legitimate contacts — the parents of their own students — can be reached.",
])

# ---------------- PART 4: PARENT ----------------
part("PART 4 — Parent: the family view")
feature("10. Parent dashboard &amp; the report they just got", [
    "Switch to the other window, log in as <b>linda / parent123</b>.",
    "Show the hero report card → click <b>View report</b> → click <b>Acknowledge</b>.",
], [
    "The parent lands on a calm, child-centred page. Here's the latest report — the one the teacher just "
    "published. When I acknowledge it, the teacher is told it was read.",
])
feature("11. Progress, calendar &amp; merged chat", [
    "Show 'Progress by area' (the coloured cards).",
    "Click a day on the week strip to reveal that day's activities.",
    "Open <b>Messages</b> — point out one room per teacher.",
], [
    "Progress is shown per development area, drawn from the IEP goals.",
    "The calendar marks busy days; click one to see what's on. And if a parent has two children with the "
    "same teacher, the chats are merged into a single room — no duplicated threads.",
])
feature("12. Notifications", [
    "Click the bell — show the unread items and that clicking one jumps straight to the page.",
], [
    "The bell ties it together: a new report, a therapy reminder, a new message — and each one deep-links "
    "to exactly the right place.",
])

# ---------------- CLOSING ----------------
part("Closing line")
story.append(Paragraph(
    "&ldquo;One login, three tailored experiences, and a single source of truth for every child — with "
    "access strictly controlled by role. Thank you — happy to take questions.&rdquo;", styles["Say"]))
story.append(PageBreak())

# ---------------- KEY TERMS ----------------
story.append(Paragraph("The terms that actually matter", styles["H1"]))
story.append(Paragraph("Don't memorise everything. Learn these eight — they cover ~90% of what you'll be "
                       "asked.", styles["Muted"]))
story.append(Spacer(1, 6))
terms([
    ("Spring Boot",
     "The Java framework the whole app is built on; it bundles the web server and wiring so the app runs as one program.",
     "“It's the backbone — Spring Boot 3, running an embedded Tomcat server.”"),
    ("Thymeleaf (server-side rendering)",
     "An HTML template engine: the server fills templates with data and sends finished pages — no separate front-end app.",
     "“Pages are rendered on the server with Thymeleaf, so there's one codebase and no JS build step.”"),
    ("MyBatis",
     "A SQL mapper: we write the SQL ourselves and it maps results to Java objects (lighter than a full ORM).",
     "“Data access is MyBatis — we control the exact SQL, kept in XML mapper files.”"),
    ("Spring Security (RBAC + BCrypt + CSRF)",
     "Handles login and access. RBAC = permissions by role; BCrypt = how passwords are hashed; CSRF = a token that blocks forged form submissions.",
     "“Login and access are Spring Security: role-based access, BCrypt-hashed passwords, CSRF-protected forms.”"),
    ("Layered architecture (Controller → Service → DAO)",
     "Code in layers, each with one job: Controller handles the request, Service holds the rules, DAO talks to the database.",
     "“It's cleanly layered — controllers stay thin, services own the rules, DAOs own the SQL.”"),
    ("Access control",
     "The rule that each role only sees its own data — enforced on the server, not just hidden in the UI.",
     "“Admins see all, teachers only their caseload, parents only their children — checked in the service layer.”"),
    ("IEP (Individualised Education Program)",
     "The personalised plan of goals for a student; the app's long-term progress tracker.",
     "“The IEP is the heart of the data — each child's goals and progress per development area.”"),
    ("HikariCP (connection pool)",
     "A reusable set of open database connections, so the app doesn't pay to open one per request.",
     "“Database connections come from a HikariCP pool — fast and efficient.”"),
])
callout("If you only remember one sentence",
    "“Spring Boot + Thymeleaf on the front, MyBatis + MySQL at the back, Spring Security around it, "
    "organised in clean layers — with every record tied to a child and locked to the right role.”")

# ---------------- BUILD ----------------
def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(BORDER); canvas.setLineWidth(0.5)
    canvas.line(20*mm, 14*mm, 190*mm, 14*mm)
    canvas.setFont("Helvetica", 8); canvas.setFillColor(INK_SOFT)
    canvas.drawString(20*mm, 9*mm, "SNE-SPIMS · Live-Demo Script")
    canvas.drawRightString(190*mm, 9*mm, "Page %d" % doc.page)
    canvas.restoreState()

doc = SimpleDocTemplate(OUT, pagesize=A4, leftMargin=20*mm, rightMargin=20*mm,
                        topMargin=18*mm, bottomMargin=20*mm,
                        title="SNE-SPIMS Live-Demo Script", author="SNE-SPIMS")
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print("WROTE", OUT)
