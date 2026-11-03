# -*- coding: utf-8 -*-
"""Generates SNE-SPIMS-Feature-Guide.pdf — a clear, presenter-friendly explanation
of every feature (what it is, who uses it, how it works, why it matters)."""
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
OUT = os.path.join(HERE, "SNE-SPIMS-Feature-Guide.pdf")

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
S("Feat", fontName="Helvetica-Bold", fontSize=14, textColor=colors.white, leading=18)
S("FeatNo", fontName="Helvetica-Bold", fontSize=14, textColor=RED_TINT, leading=18)
S("OneLine", fontName="Helvetica-Oblique", fontSize=10.5, textColor=RED_DARK, leading=14.5, spaceBefore=2, spaceAfter=6)
S("Lbl", fontName="Helvetica-Bold", fontSize=9.5, textColor=RED, leading=13, spaceBefore=5, spaceAfter=1)
S("Body", fontName="Helvetica", fontSize=10, textColor=INK, leading=14.8, spaceAfter=5)
S("Muted", fontName="Helvetica", fontSize=9, textColor=INK_SOFT, leading=12.5)
S("Cover", fontName="Helvetica-Bold", fontSize=28, textColor=RED_DARK, leading=32, alignment=TA_CENTER)
S("CoverSub", fontName="Helvetica", fontSize=13, textColor=INK_SOFT, leading=18, alignment=TA_CENTER)
S("Say", fontName="Helvetica-Oblique", fontSize=10, textColor=INK, leading=14.5)

story = []

def feature(no, title, oneline, blocks):
    """blocks: list of (label, html_text)."""
    head = Table([[Paragraph("%02d" % no, styles["FeatNo"]), Paragraph(title, styles["Feat"])]],
                 colWidths=[14*mm, 156*mm])
    head.setStyle(TableStyle([("BACKGROUND",(0,0),(-1,-1), RED),
        ("LEFTPADDING",(0,0),(-1,-1),10),("RIGHTPADDING",(0,0),(-1,-1),10),
        ("TOPPADDING",(0,0),(-1,-1),7),("BOTTOMPADDING",(0,0),(-1,-1),7),
        ("VALIGN",(0,0),(-1,-1),"MIDDLE")]))
    flow = [Spacer(1, 8), head, Paragraph("In one line: " + oneline, styles["OneLine"])]
    for label, text in blocks:
        flow.append(Paragraph(label, styles["Lbl"]))
        flow.append(Paragraph(text, styles["Body"]))
    story.append(KeepTogether(flow) if no in (1,) else Spacer(1, 0))
    # For longer features, allow them to break across pages naturally:
    if no not in (1,):
        story.append(Spacer(1, 8)); story.append(head); story.append(
            Paragraph("In one line: " + oneline, styles["OneLine"]))
        for label, text in blocks:
            story.append(Paragraph(label, styles["Lbl"]))
            story.append(Paragraph(text, styles["Body"]))

def callout(title, text):
    inner = [Paragraph("<b>%s</b>" % title, styles["Body"]), Spacer(1,2), Paragraph(text, styles["Body"])]
    t = Table([[inner]], colWidths=[170*mm])
    t.setStyle(TableStyle([("BACKGROUND",(0,0),(-1,-1), RED_TINT), ("BOX",(0,0),(-1,-1),0.5,RED),
        ("LEFTPADDING",(0,0),(-1,-1),10),("RIGHTPADDING",(0,0),(-1,-1),10),
        ("TOPPADDING",(0,0),(-1,-1),8),("BOTTOMPADDING",(0,0),(-1,-1),8)]))
    story.append(Spacer(1,4)); story.append(t); story.append(Spacer(1, 6))

# ---------------- COVER ----------------
story.append(Spacer(1, 46))
if os.path.exists(LOGO):
    try: story.append(Image(LOGO, width=64*mm, height=64*mm, kind="proportional"))
    except Exception: pass
story.append(Spacer(1, 14))
story.append(Paragraph("SNE-SPIMS", styles["Cover"]))
story.append(Paragraph("Feature Guide", styles["CoverSub"]))
story.append(Spacer(1, 8))
story.append(Paragraph("Every feature explained clearly — what it is, who uses it,<br/>how it works, and why it matters", styles["CoverSub"]))
story.append(PageBreak())

# ---------------- INTRO ----------------
story.append(Paragraph("How to read this guide", styles["H1"]))
story.append(Paragraph(
    "Each feature below follows the same four points so you can explain it confidently: "
    "<b>What it is</b>, <b>Who uses it</b>, <b>How it works</b>, and <b>Why it matters</b>. "
    "Read the italic 'In one line' first — that's the sentence to say if you only have a moment. "
    "The features build on two ideas that run through the whole app, so state them up front:",
    styles["Body"]))
callout("The two ideas behind everything",
    "<b>1. Child at the centre.</b> Every record — a goal, a report, a session, an incident, a message — "
    "is attached to a specific student, so the system always answers “how is this child doing?”.<br/>"
    "<b>2. Role-scoped access.</b> Three roles share one app — Admin, Teacher, Parent — and the server "
    "only ever shows you the data your role and your relationships allow.")

# ---------------- FEATURES ----------------
feature(1, "Login &amp; Roles",
    "One login page that quietly sends three kinds of user to three different experiences.",
    [("What it is", "A single sign-in screen shared by everyone at the school. There is no separate app per role."),
     ("Who uses it", "Everyone: the administrator, teachers, and parents."),
     ("How it works", "You enter a username and password. The system verifies the password against a securely "
        "stored hash, identifies your role (ADMIN, TEACHER, or PARENT), and redirects you to the dashboard built "
        "for that role. From then on, the left icon rail only shows the menus your role is allowed to use."),
     ("Why it matters", "It keeps the experience simple and safe: people never see controls that aren't theirs, "
        "and the same secure front door serves the whole school.")])

feature(2, "Role-aware Dashboards",
    "A different home screen for each role — statistics for the admin, a worklist for the teacher, a child hub for the parent.",
    [("What it is", "The landing page after login. Its content adapts entirely to who you are."),
     ("Who uses it", "All three roles, each seeing their own version."),
     ("How it works", "The <b>admin</b> dashboard shows whole-school statistics: total students, active IEPs, "
        "recent incidents, staff counts, this week's therapy, a disability breakdown, upcoming events and "
        "announcements. The <b>teacher</b> dashboard is an actionable worklist: their students, unread messages, "
        "this week's sessions, and which students still need a daily report. The <b>parent</b> dashboard centres on "
        "one child: the latest report, progress by area, the week's activities, and announcements. Every number is "
        "pulled live from the database — nothing is hard-coded."),
     ("Why it matters", "Each person opens the app and immediately sees what they need to act on, with no clutter "
        "from features meant for someone else.")])

feature(3, "Students &amp; Profiles",
    "The roster of children and each child's profile — the spine the rest of the system hangs off.",
    [("What it is", "The list of students and their individual profile pages (code, disability type, support "
        "level, class group, enrolment)."),
     ("Who uses it", "Admin (the full roster, plus add/edit/disable) and teachers (only the students assigned to "
        "them). Parents don't browse the roster; they reach their own child from their dashboard."),
     ("How it works", "Each student record links to everything else — their IEP, daily reports, therapy sessions, "
        "and incidents — through the student's id. Two kinds of assignment connect people to children: "
        "<b>teacher–student</b> (a teacher's caseload) and <b>parent–student</b> (a family link). These assignments "
        "are exactly what the access rules check."),
     ("Why it matters", "Because everything is organised around the child rather than around features, staff and "
        "parents always get a complete, joined-up picture of one student.")])

feature(4, "IEP Program (Individualised Education Program)",
    "Each child's personalised plan of goals — and the app's main long-term progress tracker.",
    [("What it is", "A formal, individualised plan describing a student's present levels, the accommodations they "
        "need, and a set of goals to work toward."),
     ("Who uses it", "Teachers create and update it; parents view their child's plan; admins can see it across the "
        "school."),
     ("How it works", "A student has an active IEP program containing several <b>goals</b>. Each goal sits in one of "
        "five <b>development areas</b> — Academic, Communication, Social/Emotional, Self-Care, and Motor — and carries "
        "a target date and a <b>progress percentage</b>. As a teacher updates a goal's progress over time, those "
        "values are kept as history, which the app turns into a progress trend. The dashboard summarises the latest "
        "progress per area."),
     ("Why it matters", "The IEP is the heart of special-needs education. It turns vague 'how is the child doing?' "
        "into concrete, measurable goals that everyone — school and family — can follow in the same place.")])

feature(5, "Daily Reports",
    "A short daily note from a teacher about a child's day, published to the parent.",
    [("What it is", "A brief record of a single day: a summary, the child's mood, and a few rubric ratings."),
     ("Who uses it", "Teachers write them; parents read them."),
     ("How it works", "A teacher writes a report for a student, picks a mood, and scores a few <b>rubric</b> criteria "
        "on a scale (for example Emerging → Developing → Proficient → Mastered). It can be saved as a draft or "
        "<b>published</b>. The instant it's published, the system raises an internal event that creates a "
        "<b>notification</b> for the child's parent. The parent opens the report and can <b>acknowledge</b> it, which "
        "marks it read and lets the teacher know it was seen."),
     ("Why it matters", "It keeps families genuinely in the loop, day by day, and the acknowledgement closes the "
        "loop so teachers know the message landed.")])

feature(6, "Therapy Sessions",
    "Scheduling specialist sessions — with an automatic check that nothing clashes.",
    [("What it is", "Planned therapy appointments (speech, occupational, physical, counselling, behavioural) in a "
        "specific room at a specific time."),
     ("Who uses it", "Teachers/staff schedule and manage them; parents see their child's upcoming sessions; admins "
        "see them school-wide."),
     ("How it works", "When a new session is booked, the system runs a <b>three-way conflict check</b>: the same "
        "<b>student</b>, the same <b>therapist</b>, and the same <b>room</b> cannot be double-booked for overlapping "
        "times. If any of the three clashes, the save is refused with a clear message. Sessions carry a status "
        "(Scheduled, Completed, Cancelled), and the system can issue reminders for upcoming ones."),
     ("Why it matters", "It prevents the everyday scheduling mistakes — two children sent to the same therapist, or "
        "two sessions in one room — before they happen, instead of discovering them on the day.")])

feature(7, "Behaviour Incidents",
    "A careful record of concerning behaviour, with control over what families see.",
    [("What it is", "A logged event describing what happened, how serious it was, and what staff did about it."),
     ("Who uses it", "Teachers record them; admins review them school-wide; parents see only the ones marked "
        "visible to them."),
     ("How it works", "Each incident has a date, a location, a description, a <b>severity</b> (Low, Medium, High), "
        "the action taken, and a <b>parent-visible</b> switch. That switch decides whether the family sees the "
        "incident at all — sensitive cases can be kept staff-only. The dashboard surfaces recent incidents so "
        "patterns don't go unnoticed."),
     ("Why it matters", "It gives the school an honest, consistent behaviour log while respecting that some details "
        "need professional judgement before they reach a parent.")])

feature(8, "Messages (Parent–Teacher Chat)",
    "A focused chat between a parent and their child's teacher — not an open inbox.",
    [("What it is", "A chat-room style messaging area, styled like a modern messenger rather than email."),
     ("Who uses it", "Teachers and parents. (Admins deliberately stay out of private parent–teacher conversations.)"),
     ("How it works", "Conversations are scoped by relationship: a parent can only message the teachers of their "
        "own children, and a teacher only sees the parents of the students they teach. For a <b>teacher</b>, there's "
        "one room per parent–child pair. For a <b>parent</b> with two children taught by the same teacher, the chats "
        "are <b>merged into a single room</b> so there are no duplicated threads. The server checks every message is "
        "between a legitimate pair before delivering it."),
     ("Why it matters", "It makes school–home communication easy and personal, while the strict scoping keeps it "
        "private and prevents messaging the wrong people.")])

feature(9, "Calendar",
    "One shared calendar that merges everything happening for a child or the school.",
    [("What it is", "A combined view of dates and activities."),
     ("Who uses it", "All roles, each seeing the slice that's relevant to them."),
     ("How it works", "The calendar blends three sources into one timeline: <b>therapy sessions</b>, <b>IEP goal "
        "target dates</b>, and <b>school-wide events</b>. The data is scoped by role — a parent sees their child's "
        "activities, a teacher sees their students', and the admin sees the whole school. On the dashboards, a "
        "compact week strip highlights busy days, and clicking a day reveals exactly what's on."),
     ("Why it matters", "Instead of checking several places, everyone sees a single, coherent picture of what's "
        "coming up.")])

feature(10, "Announcements",
    "School notices, aimed at the right audience.",
    [("What it is", "Short notices the school wants people to see."),
     ("Who uses it", "The admin writes, edits, and removes them; teachers and parents read the ones meant for them."),
     ("How it works", "Each announcement is targeted to an audience — <b>everyone</b>, <b>teachers</b>, or "
        "<b>parents</b> — and the right notices appear as a widget on each person's dashboard."),
     ("Why it matters", "It's a simple, controlled way to reach the whole community without spamming people with "
        "messages that don't concern them.")])

feature(11, "Notifications",
    "A bell that tells you what changed and takes you straight there.",
    [("What it is", "Personal alerts shown on a bell icon with an unread count."),
     ("Who uses it", "All roles."),
     ("How it works", "Certain actions create a notification automatically — a newly published daily report, a "
        "therapy reminder, or a new message. The unread count refreshes on its own, and clicking a notification "
        "<b>deep-links</b> to the exact page it refers to (the specific report, the calendar, or the right chat room)."),
     ("Why it matters", "Nothing important gets missed, and people reach the relevant page in one click instead of "
        "hunting for it.")])

feature(12, "User Management",
    "The admin's control panel for accounts.",
    [("What it is", "Tools to manage every account in the system."),
     ("Who uses it", "The admin only."),
     ("How it works", "The admin can create accounts, edit details, assign roles, reset passwords, and "
        "activate/deactivate users. New passwords are stored only as secure hashes, never as plain text."),
     ("Why it matters", "It keeps control of who can access the system in one trusted place — essential for a "
        "school handling sensitive information about children.")])

feature(13, "Account &amp; Security",
    "Your own profile and the protections that run quietly underneath everything.",
    [("What it is", "Each user's personal account page, plus the security that wraps the whole app."),
     ("Who uses it", "Everyone manages their own account; the security applies to all."),
     ("How it works", "From the account page a user can update their profile and change their password. Underneath, "
        "passwords are <b>BCrypt-hashed</b>, every form is protected against forged submissions (CSRF), and access is "
        "enforced on the server by a single rule: <b>admins</b> see everything, <b>teachers</b> only their caseload, "
        "and <b>parents</b> only their own children. The interface hides what you can't use, but the server is the "
        "real gatekeeper."),
     ("Why it matters", "Children's records are sensitive. Enforcing access in one place — on the server, by "
        "relationship — is what makes the whole system trustworthy.")])

# ---------------- CLOSING ----------------
story.append(Spacer(1, 10))
callout("The one-paragraph summary",
    "SNE-SPIMS brings a special-needs school's information into one place, organised around each child. Teachers "
    "plan and track learning with IEP goals, keep families close with daily reports, schedule therapy without "
    "clashes, and log behaviour responsibly. Parents follow their child's progress, read and acknowledge reports, "
    "and chat with the right teacher. The admin runs the school-wide view — people, students, announcements, and "
    "statistics. A shared calendar and a notification bell tie it together, and strict role-based access keeps "
    "every record in the right hands.")

# ---------------- BUILD ----------------
def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(BORDER); canvas.setLineWidth(0.5)
    canvas.line(20*mm, 14*mm, 190*mm, 14*mm)
    canvas.setFont("Helvetica", 8); canvas.setFillColor(INK_SOFT)
    canvas.drawString(20*mm, 9*mm, "SNE-SPIMS · Feature Guide")
    canvas.drawRightString(190*mm, 9*mm, "Page %d" % doc.page)
    canvas.restoreState()

doc = SimpleDocTemplate(OUT, pagesize=A4, leftMargin=20*mm, rightMargin=20*mm,
                        topMargin=18*mm, bottomMargin=20*mm,
                        title="SNE-SPIMS Feature Guide", author="SNE-SPIMS")
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print("WROTE", OUT)
