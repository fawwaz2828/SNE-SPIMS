# -*- coding: utf-8 -*-
"""Generates SNE-SPIMS-Speaker-Script.pdf — a word-for-word presentation script
to memorise, in simple English, ordered Parent -> Teacher -> Admin."""
import os
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
                                PageBreak, Image, KeepTogether)

HERE = os.path.dirname(os.path.abspath(__file__))
LOGO = os.path.join(HERE, "..", "src", "main", "resources", "static", "images", "lunabridge.png")
OUT = os.path.join(HERE, "SNE-SPIMS-Speaker-Script.pdf")

RED      = colors.HexColor("#B23A2E")
RED_DARK = colors.HexColor("#7A2820")
RED_TINT = colors.HexColor("#FBEDEA")
INK      = colors.HexColor("#2B2320")
INK_SOFT = colors.HexColor("#6B5E58")
BORDER   = colors.HexColor("#E7DED9")

styles = getSampleStyleSheet()
def S(name, **kw):
    styles.byName[name] = ParagraphStyle(name, parent=styles["Normal"], **kw)

S("Part", fontName="Helvetica-Bold", fontSize=13, textColor=colors.white, leading=17)
S("Tag", fontName="Helvetica-Bold", fontSize=9, textColor=RED_TINT, leading=12)
S("Say", fontName="Helvetica", fontSize=12.5, textColor=INK, leading=19, spaceAfter=9)
S("Tip", fontName="Helvetica-Oblique", fontSize=9.5, textColor=INK_SOFT, leading=13, spaceAfter=4)
S("Cover", fontName="Helvetica-Bold", fontSize=27, textColor=RED_DARK, leading=31, alignment=TA_CENTER)
S("CoverSub", fontName="Helvetica", fontSize=13, textColor=INK_SOFT, leading=18, alignment=TA_CENTER)

story = []

def part(label, secs):
    p = Paragraph(label, styles["Part"])
    s = Paragraph(secs, styles["Tag"])
    t = Table([[p, s]], colWidths=[130*mm, 40*mm])
    t.setStyle(TableStyle([("BACKGROUND",(0,0),(-1,-1), RED),
        ("LEFTPADDING",(0,0),(-1,-1),12),("RIGHTPADDING",(0,0),(-1,-1),12),
        ("TOPPADDING",(0,0),(-1,-1),7),("BOTTOMPADDING",(0,0),(-1,-1),7),
        ("VALIGN",(0,0),(-1,-1),"MIDDLE"),("ALIGN",(1,0),(1,0),"RIGHT")]))
    story.append(Spacer(1, 8)); story.append(t); story.append(Spacer(1, 6))

def say(text):
    story.append(Paragraph(text, styles["Say"]))

def tip(text):
    story.append(Paragraph("▶ " + text, styles["Tip"]))

# ---------------- COVER ----------------
story.append(Spacer(1, 50))
if os.path.exists(LOGO):
    try: story.append(Image(LOGO, width=62*mm, height=62*mm, kind="proportional"))
    except Exception: pass
story.append(Spacer(1, 14))
story.append(Paragraph("SNE-SPIMS", styles["Cover"]))
story.append(Paragraph("Presentation Script — to memorise", styles["CoverSub"]))
story.append(Spacer(1, 8))
story.append(Paragraph("Simple English &bull; Parent &rarr; Teacher &rarr; Admin", styles["CoverSub"]))
story.append(Spacer(1, 24))
story.append(Paragraph("Read the lines out loud. The grey lines are reminders of what to click.",
                       styles["CoverSub"]))
story.append(PageBreak())

# ---------------- OPENING ----------------
part("OPENING", "~20 sec")
say("&ldquo;Good morning, everyone. Today I will show you our website. It is called <b>Luna Bridge</b>. "
    "It is a system for a <b>special-needs school</b>.&rdquo;")
say("&ldquo;Three kinds of people use it: <b>parents, teachers, and one admin</b>. They all use the same "
    "website, but each person sees different things.&rdquo;")
say("&ldquo;Let me start from the <b>parent&rsquo;s</b> side.&rdquo;")
tip("Show the login page, then log in as a parent (linda / parent123).")

# ---------------- PARENT ----------------
part("1. PARENT DASHBOARD", "~60 sec")
say("&ldquo;This is the <b>parent&rsquo;s</b> dashboard. A parent logs in to follow their child.&rdquo;")
say("&ldquo;At the top, the parent sees the <b>latest report</b> from the teacher. They click <i>View report</i> "
    "to read it. The report shows a short summary of the day, the child&rsquo;s <b>mood</b>, and a few ratings. "
    "After reading, the parent clicks <b>Acknowledge</b> &mdash; this tells the teacher that they have seen it.&rdquo;")
say("&ldquo;Below that is <b>Progress by area</b>. This shows how the child is doing in five areas, like "
    "communication and motor skills. These numbers come from the child&rsquo;s learning goals.&rdquo;")
say("&ldquo;On the right, there is a small <b>calendar</b> for this week. Busy days are marked. If I click a day, "
    "I can see what happens that day, like a therapy session.&rdquo;")
say("&ldquo;The parent can also open <b>Messages</b> to chat with the teacher. If a parent has two children with "
    "the same teacher, the chats are <b>joined into one room</b>.&rdquo;")
say("&ldquo;So for the parent, everything answers one question: <b>how is my child doing today?</b>&rdquo;")

# ---------------- TEACHER ----------------
part("2. TEACHER DASHBOARD", "~70 sec")
tip("Log out, then log in as a teacher (james / teacher123).")
say("&ldquo;Now let me log in as a <b>teacher</b>.&rdquo;")
say("&ldquo;The teacher&rsquo;s dashboard is a <b>to-do list</b>. It shows how many students they have, and how "
    "many messages are unread. The most useful part is <b>Reports to write</b> &mdash; it shows which students "
    "still need a report.&rdquo;")
say("&ldquo;The teacher clicks <b>Write report</b>, writes a short summary, picks a mood, gives a few ratings, "
    "and clicks <b>Publish</b>. The moment they publish, the <b>parent gets a notification</b>.&rdquo;")
say("&ldquo;The teacher can also open a student to see the <b>IEP</b>. The IEP is the child&rsquo;s "
    "<b>learning plan</b>. It has goals, and each goal has a <b>progress percent</b>.&rdquo;")
say("&ldquo;The teacher can also schedule <b>therapy</b>. When they book a session, the system <b>checks for "
    "conflicts</b> &mdash; the same student, therapist, or room cannot be booked twice at the same time. If there "
    "is a clash, it stops you.&rdquo;")
say("&ldquo;The teacher can also record a <b>behaviour incident</b> &mdash; how serious it was, and whether the "
    "parent can see it.&rdquo;")
say("&ldquo;So for the teacher, this system is their <b>daily work tool</b>.&rdquo;")

# ---------------- ADMIN ----------------
part("3. ADMIN DASHBOARD", "~45 sec")
tip("Log out, then log in as the admin (admin / admin123).")
say("&ldquo;Last, let me log in as the <b>admin</b>.&rdquo;")
say("&ldquo;The admin sees the <b>whole school</b>. The dashboard shows numbers: total students, active IEPs, "
    "recent incidents, the number of teachers and parents, and therapy sessions this week.&rdquo;")
say("&ldquo;The admin manages <b>students, user accounts, and announcements</b>. An announcement can be sent to "
    "<b>everyone, only teachers, or only parents</b>.&rdquo;")
say("&ldquo;The admin does <b>not</b> join the private chats between parents and teachers. That is on purpose, "
    "to keep them private.&rdquo;")
say("&ldquo;So for the admin, this system is a <b>control room</b> for the whole school.&rdquo;")

# ---------------- CLOSING ----------------
part("CLOSING", "~15 sec")
say("&ldquo;So that is our system. <b>One website, three roles.</b> The parent follows their child. The teacher "
    "does their daily work. The admin runs the school.&rdquo;")
say("&ldquo;And the data is kept <b>safe</b> &mdash; each person only sees what they are allowed to see.&rdquo;")
say("&ldquo;Thank you. I am happy to answer your questions.&rdquo;")

# ---------------- BUILD ----------------
def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(BORDER); canvas.setLineWidth(0.5)
    canvas.line(20*mm, 14*mm, 190*mm, 14*mm)
    canvas.setFont("Helvetica", 8); canvas.setFillColor(INK_SOFT)
    canvas.drawString(20*mm, 9*mm, "SNE-SPIMS · Presentation Script")
    canvas.drawRightString(190*mm, 9*mm, "Page %d" % doc.page)
    canvas.restoreState()

doc = SimpleDocTemplate(OUT, pagesize=A4, leftMargin=22*mm, rightMargin=22*mm,
                        topMargin=18*mm, bottomMargin=20*mm,
                        title="SNE-SPIMS Presentation Script", author="SNE-SPIMS")
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print("WROTE", OUT)
