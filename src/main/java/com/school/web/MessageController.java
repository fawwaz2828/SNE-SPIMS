package com.school.web;

import com.school.model.Assignment;
import com.school.model.Conversation;
import com.school.model.Message;
import com.school.model.Student;
import com.school.model.User;
import com.school.notify.NotificationService;
import com.school.security.SecurityUtils;
import com.school.service.MessageService;
import com.school.service.StudentService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Parent-teacher messaging anchored to a student. */
@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messages;
    private final StudentService students;
    private final NotificationService notifications;

    public MessageController(MessageService messages, StudentService students,
                             NotificationService notifications) {
        this.messages = messages;
        this.students = students;
        this.notifications = notifications;
    }

    /** Rooms list: existing chat threads plus every contact the user could chat with. */
    @GetMapping
    public String rooms(Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("conversations", buildRooms(user));
        model.addAttribute("me", user.getId());
        return "messages/rooms";
    }

    /**
     * Builds the room list, role-aware:
     *  • Teacher — one room per (parent, student): the parents of every student they
     *    teach, each labelled with the child, so a teacher can chat per child.
     *  • Parent — one room per teacher (merged across children, no child label): a
     *    parent chats with a teacher as a person even when several of their children
     *    share that teacher.
     * Existing threads come first; contacts with no messages yet are appended.
     */
    private List<Conversation> buildRooms(User user) {
        if (user.isParent()) {
            return parentRooms(user);
        }
        if (user.isTeacher()) {
            return teacherRooms(user);
        }
        // Admins don't use direct messaging; show only existing threads (no contacts).
        return new ArrayList<>(messages.conversations(user));
    }

    /** One room per (parent, student), labelled with the child. */
    private List<Conversation> teacherRooms(User user) {
        Map<String, Conversation> byKey = new LinkedHashMap<>();
        for (Conversation c : messages.conversations(user)) {
            byKey.put(c.getOtherId() + ":" + c.getStudentId(), c);
        }
        for (Student s : students.visibleRoster(user, null, null, null, null)) {
            for (Assignment a : messages.allowedRecipients(user, s.getId())) {
                String key = a.getUserId() + ":" + s.getId();
                if (byKey.containsKey(key)) {
                    continue;
                }
                Conversation c = new Conversation();
                c.setOtherId(a.getUserId());
                c.setOtherName(a.getUserName());
                c.setOtherRole(a.getUserRole());
                c.setStudentId(s.getId());
                c.setStudentName(s.getFullName());
                c.setUnread(0);
                byKey.put(key, c);
            }
        }
        return new ArrayList<>(byKey.values());
    }

    /** One room per teacher, merged across the parent's children (no child label). */
    private List<Conversation> parentRooms(User user) {
        Map<Long, Conversation> byTeacher = new LinkedHashMap<>();
        // conversations() is newest-first, so the first row per teacher is the latest.
        for (Conversation c : messages.conversations(user)) {
            Conversation ex = byTeacher.get(c.getOtherId());
            if (ex == null) {
                c.setStudentId(null);     // merged room — drop the child context
                c.setStudentName(null);
                byTeacher.put(c.getOtherId(), c);
            } else {
                ex.setUnread(ex.getUnread() + c.getUnread());
            }
        }
        for (Student s : students.visibleRoster(user, null, null, null, null)) {
            for (Assignment a : messages.allowedRecipients(user, s.getId())) {
                if (byTeacher.containsKey(a.getUserId())) {
                    continue;
                }
                Conversation c = new Conversation();
                c.setOtherId(a.getUserId());
                c.setOtherName(a.getUserName());
                c.setOtherRole(a.getUserRole());
                c.setUnread(0);
                byTeacher.put(a.getUserId(), c);
            }
        }
        return new ArrayList<>(byTeacher.values());
    }

    /** A child of the parent that {@code teacherId} teaches, or null if none. */
    private Long resolveStudentForParent(User parent, long teacherId) {
        for (Student s : students.visibleRoster(parent, null, null, null, null)) {
            for (Assignment a : messages.allowedRecipients(parent, s.getId())) {
                if (a.getUserId() == teacherId) {
                    return s.getId();
                }
            }
        }
        return null;
    }

    /** Open one chat thread (rooms list on the left, the conversation on the right). */
    @GetMapping("/with/{otherId}")
    public String thread(@PathVariable long otherId,
                         @RequestParam(required = false) Long studentId,
                         Model model) {
        User user = SecurityUtils.currentUser();
        boolean parent = user.isParent();

        // Parent: merged thread across all children. Teacher: per-student thread.
        List<Message> thread = parent
                ? messages.threadWith(user, otherId)
                : messages.thread(user, otherId, studentId);
        List<Conversation> rooms = buildRooms(user);

        // Resolve the other person's name (and, for a teacher, the child) from the
        // thread, or — for a not-yet-started chat — from the matching room contact.
        String otherName = null;
        String studentName = null;
        for (Message m : thread) {
            otherName = (m.getSenderId() == otherId) ? m.getSenderName() : m.getRecipientName();
            studentName = m.getStudentName();
            break;
        }
        if (otherName == null) {
            for (Conversation c : rooms) {
                if (c.getOtherId() == otherId) {
                    otherName = c.getOtherName();
                    if (!parent) {
                        studentName = c.getStudentName();
                    }
                    break;
                }
            }
        }

        // Whether the reply box is shown, and which student the message anchors to.
        boolean canReply;
        Long formStudentId;
        if (parent) {
            formStudentId = resolveStudentForParent(user, otherId);
            canReply = formStudentId != null;
            studentName = null;                 // merged room — never label the child
        } else {
            formStudentId = studentId;
            canReply = messages.canMessage(user, otherId, studentId);
        }

        model.addAttribute("conversations", rooms);
        model.addAttribute("thread", thread);
        model.addAttribute("me", user.getId());
        model.addAttribute("otherId", otherId);
        model.addAttribute("otherName", otherName);
        model.addAttribute("studentId", formStudentId);
        model.addAttribute("studentName", studentName);
        model.addAttribute("canReply", canReply);
        return "messages/thread";
    }

    /**
     * Send a message inside a thread (no subject — chat style). A teacher posts the
     * student explicitly; a parent omits it and we anchor to a child that the teacher
     * teaches. The thread is reopened by the other party (parents land on the merged
     * room).
     */
    @PostMapping("/send")
    public String send(@RequestParam long otherId,
                       @RequestParam(required = false) Long studentId,
                       @RequestParam String body,
                       RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        boolean parent = user.isParent();
        Long sid = (studentId != null) ? studentId : resolveStudentForParent(user, otherId);
        if (sid == null) {
            ra.addFlashAttribute("flashError", "Could not determine which student this chat is about.");
            return "redirect:/messages";
        }
        messages.sendChat(user, otherId, sid, body);
        // Recipient deep-link: a parent recipient sees the merged room; a teacher sees
        // the per-student room.
        // (otherId is the recipient here; the recipient is a parent when the sender is a teacher.)
        // Deep-link includes the student so a teacher recipient lands on the right
        // per-student thread (a parent recipient's merged view ignores it harmlessly).
        notifications.notifyUser(otherId, "MESSAGE", "New message",
                user.getFullName() + " sent you a message.",
                "/messages/with/" + user.getId() + "?studentId=" + sid, sid);
        return parent
                ? "redirect:/messages/with/" + otherId
                : "redirect:/messages/with/" + otherId + "?studentId=" + sid;
    }

    /** Start a new conversation: pick the student, then the recipient. */
    @GetMapping("/compose")
    public String compose(@RequestParam(required = false) Long studentId, Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("roster", students.visibleRoster(user, null, null, null, null));
        model.addAttribute("studentId", studentId);
        if (studentId != null) {
            model.addAttribute("recipients", messages.allowedRecipients(user, studentId));
        }
        return "messages/compose";
    }

    /** Legacy deep-links (/messages/{id}) → redirect into the relevant thread. */
    @GetMapping("/{id}")
    public String view(@PathVariable long id) {
        User user = SecurityUtils.currentUser();
        Message m = messages.open(user, id);
        long other = (m.getSenderId() == user.getId()) ? m.getRecipientId() : m.getSenderId();
        String s = m.getStudentId() == null ? "" : "?studentId=" + m.getStudentId();
        return "redirect:/messages/with/" + other + s;
    }
}
