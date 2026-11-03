# SNE-SPIMS — Design Guide

Design system for **SNE-SPIMS** (Special Needs Education — Student Progress & Information Management System).

This document describes the **live design system** of the Spring Boot app. It is implemented as
a token + component layer, not a one-off prototype, so every page inherits it:

| Where | What |
|---|---|
| `src/main/resources/static/css/app.css` | Design tokens (`oklch` CSS variables) + restyle of every Bootstrap 5 component (buttons, cards, tables, badges, forms, alerts, dropdowns…). Bridges Bootstrap's `--bs-primary` onto the clay accent. |
| `src/main/resources/templates/layout/main.html` | The shared shell: 80px icon **rail** + topbar + full-width content surface (flat — no floating card). Every Thymeleaf page `layout:decorate`s it, so the system applies app-wide. |
| `src/main/resources/templates/login.html` | Standalone sign-in page (no decorator) — styled directly from the same tokens. |

> To change the look globally, edit those two files. (Deploy note: the app serves from
> `target/classes`; copy changed files there or rebuild — see the project README/memory.)

---

## 1. Design Principles

Context: an app for a **special-needs school**, used by parents and staff. Because of this, the design is:

1. **Calm & warm** — soft colors, no harsh primary colors, generous white space.
2. **Easy to read** — large typography, sufficient contrast, clear and unabbreviated labels.
3. **Simple & focused** — one main accent color, a limited number of elements, no decorative numbers/icons that serve no purpose.
4. **Positive & encouraging** — achievements are written as "3 of 4 IEP goals on track", not a discouraging empty state like "0 / 0".
5. **Child at the center** — information revolves around each child's profile, not a list of features.

---

## 2. Color

All colors are defined with `oklch()` so lightness & chroma stay consistent and are easy to derive.
These values are the source of truth and match the `:root` tokens in `app.css`.

### Neutrals (warm)
| Token | Value | Usage |
|---|---|---|
| `--bg` | `oklch(0.97 0.009 50)` | Content / input background |
| `--surface` | `oklch(0.998 0.003 60)` | Card surface (warm white) |
| `--ink` | `oklch(0.31 0.018 40)` | Primary text (warm near-black) |
| `--ink-soft` | `oklch(0.55 0.018 45)` | Secondary text |
| `--border` | `oklch(0.92 0.012 55)` | Lines & card borders |

### Primary accent — Soft Red (clay)
| Token | Value | Usage |
|---|---|---|
| `--red` | `oklch(0.59 0.155 28)` | Active rail button, highlight, links, primary buttons |
| `--red-deep` | `oklch(0.5 0.16 28)` | Hover / text over a tint |
| `--red-tint` | `oklch(0.955 0.03 32)` | Soft background, subtle hover, calm badges |

> The red is intentionally **clay/terracotta** (medium chroma) to feel warm & calming — not an alarming red.
> Bootstrap's `--bs-primary` is remapped to `--red`, so all `btn-primary` / `text-primary` follow it.

### Domain colors (muted, harmonious)
Used for the **5 IEP development areas** in the data model (Academic, Communication, Social/Emotional,
Self-Care, Motor). They share similar lightness & chroma and differ only in hue — keeping the palette calm.

| Domain | Line/Icon | Card background |
|---|---|---|
| Academic | `--d-academic` `oklch(0.6 0.12 250)` | `--c-blue-bg` `oklch(0.95 0.032 250)` |
| Communication | `--d-comm` `oklch(0.58 0.1 165)` | `--c-green-bg` `oklch(0.952 0.038 165)` |
| Social / Emotional | `--d-social` `oklch(0.64 0.12 60)` | `--c-amber-bg` `oklch(0.955 0.045 72)` |
| Self-Care | `--d-selfcare` `oklch(0.58 0.13 300)` | `--c-violet-bg` `oklch(0.95 0.036 300)` |
| Motor | `--d-motor` `oklch(0.6 0.12 200)` | `--c-cyan-bg` `oklch(0.95 0.034 200)` |

Each is exposed as a helper class (`.domain-academic`, `.domain-comm`, `.domain-social`,
`.domain-selfcare`, `.domain-motor`) that sets `--domain` and `--domain-bg`.

### App background
The app fills the viewport: the page background is the calm neutral `--bg`, and the shell sits on `--surface`. There is **no coloured wash and no floating/rounded app card** — content lies flat, matching the parent dashboard. Soft clay gradients are reserved for in-page **hero banners** only, not the page background.

---

## 3. Typography

- **Font:** [Lexend](https://fonts.google.com/specimen/Lexend) — designed to reduce reading fatigue, well suited to an accessibility context. Loaded from Google Fonts in `app.css` / the layout head.
- **Mono (placeholder):** JetBrains Mono — only for image-slot labels.

| Role | Size | Weight | Notes |
|---|---|---|---|
| Page / hero heading | 22–26px | 800 | letter-spacing -0.01em |
| Section heading (`h3`) | 18px | 800 | |
| Card / session title | 15.5–16.5px | 700 | |
| Body / description | 14–15.5px | 400 | line-height 1.45–1.5 |
| Secondary / meta label | 13–13.5px | 600 | color `--ink-soft` |

Principle: **no important text below ~13px**; primary body text ≥14px (base 15px).

---

## 4. Shape, Spacing & Shadow

- **Radius:** content cards `18–20px`, buttons/chips `11–16px`, pill/avatar `999px`. The **app shell is not rounded** (full-bleed surface).
- **Spacing:** grid & flex always use `gap` (16–24px between blocks). Card padding 18px; content padding 24–30px.
- **Shadows:** soft & warm, used sparingly —
    - Cards: `0 1px 2px rgba(90,40,30,.06)`
    - Active/accent button: `0 8px 18px -8px var(--red)`
- **Lines:** 1px `--border` to separate, instead of hard shadows.

---

## 5. Layout

The shell in `layout/main.html` wraps **every** page — an icon rail + topbar + a full-width content
surface (flat, no floating card), the same flat layout as the parent dashboard:

```
┌──────────────────────────────────────────────────────────────┐
│ rail │  [ search students ...... ]            bell   avatar   │
│ (80) ├──────────────────────────────────────────────────────  │
│  ▣   │                                                         │
│  ⌂   │   flash messages                                        │
│  ▦   │   ┌──────────────────────────────────────────────────┐ │
│  📅  │   │  page content (layout:fragment="content")         │ │
│  ✉   │   │  …scrolls within the card…                        │ │
│  ▤   │   └──────────────────────────────────────────────────┘ │
│  ⎋   │   footer                                                │
└──────────────────────────────────────────────────────────────┘
```

- **Rail (80px):** icon-only, role-aware (`sec:authorize`). Logo → Home, Students/My Children,
  Calendar, Attendance, Messages (teacher/parent), Reports, plus admin tools (Users, IEP Goal Bank,
  Announcements), with **logout pinned at the bottom**. The active item is detected from the request
  path and highlighted with `--red`.
- **Topbar:** staff get a student quick-search pill (`GET /students`); parents get a greeting. Plus
  notification bell (live unread badge) and an account dropdown.
- **Shell:** `80px` rail + `1fr` content, **capped at `max-width: 1440px` and centred** so every
  page shares the same content width (sides show the neutral `--bg` on wide screens). Surface
  `--surface`; the rail is sticky full-height, content scrolls. **No coloured wash, no rounded
  floating card.** Collapses to a horizontal top strip below 720px.

The original "e-learning dashboard" reference layout (`1fr 364px` two-column, no-scroll) still informs
the **dashboard page** specifically; other pages reuse the same shell with their own content.

---

## 6. Core Components

These are the restyled Bootstrap components from `app.css` — existing markup picks them up automatically.

| Component | Description |
|---|---|
| **Rail button** | 50×50, rounded square (radius 15) at rest; morphs to a **circle** on hover/active. Active = `--red` background + white icon (flat, no glow). Hover = `--red-tint`. |
| **Search bar** | Pill, `--bg` background, icon + input, focus → `--red` border. |
| **Card** | `--surface`, radius 18–20, 1px `--border`, soft warm shadow; transparent header with a 1px divider. |
| **Button** | Radius 14 (sm 11), weight 600. `btn-primary` = clay with accent shadow; `btn-secondary` = neutral tinted. |
| **Table** | Transparent rows, `--red-tint` hover, uppercase `--ink-soft` header on `--bg`. |
| **Badge** | Calm pill, tinted background + readable colored text (status stays distinguishable, not loud). |
| **Progress bar** | `--red` fill on `--bg` track — always paired with a visible percentage. |
| **Calendar widget** | Month nav ‹ › + a 7-day strip; today highlighted in `--red`. |
| **Session card** | Colored tags, title, short description, meta (time, duration), staff avatars, "View details". |

---

## 7. Content Mapping (e-learning template → SNE-SPIMS)

| Original template | Becomes |
|---|---|
| "Your progress" (courses) | **Progress this month** (5 IEP development domains, per child) |
| "Top articles for you" | **Resources for you** (guides for parents) |
| "Upcoming courses" + price | **Upcoming sessions** (therapy/class) + time |
| Student avatars | Staff/therapist avatars (initials, not fake photos) |

---

## 8. Accessibility

- Lexend font + large sizes for readability.
- Touch targets ≥ 44px (rail 50px, round icon buttons 44px).
- Primary text `--ink` over light surfaces meets good readability/contrast.
- Icons are decorative; information is conveyed through text, not color alone (every progress bar shows a percentage; status badges carry a word, not just a hue).
- Decoration (blobs, background wash) carries no meaning, so it is safe to ignore.

---

## 9. Assets & Data

- **No real images yet.** Resource cards use a striped placeholder labeled `illustration` (mono font) — ready to be swapped for real artwork.
- **Avatars** use colored initials (e.g. "JW", "LC") to avoid showing fake photos.
- **Real data** comes from the MySQL schema + `db/seed.sql` (students such as Ryan Walker, Daisy Cooper, Jacob Turner; staff James/Laura; parents Robert/Linda). No fictional placeholder records remain in the design.

---

## 10. Files

| File | Contents |
|---|---|
| `src/main/resources/static/css/app.css` | **Design system** — tokens + Bootstrap component restyle. Edit colors/shape/typography here. |
| `src/main/resources/templates/layout/main.html` | **App shell** — icon rail + topbar + floating card; decorated by every page. |
| `src/main/resources/templates/login.html` | Standalone sign-in page. |
| `src/main/resources/templates/**` | Per-page content fragments (Bootstrap markup that inherits the system). |
| `docs/legacy-jsp/` | Archived pre-migration JSP UI, for reference only (not built). |

---

*The color system uses oklch so it is easy to extend. To add a new domain/color, keep lightness ~0.6 and chroma ~0.12 and change only the hue to stay harmonious — then add a `--d-*` token, a `--c-*-bg` tint, and a `.domain-*` helper in `app.css`.*
