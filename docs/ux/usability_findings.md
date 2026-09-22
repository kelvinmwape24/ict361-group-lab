# Usability findings — Activity A

**Status: not yet run. Every row below is a blank to be filled from a real
session.** Nothing here may be written from memory or invented; Activity A is
15 marks and the one part of the lab that no amount of code earns.

What the brief asks for:

- six wireframes
- three classmates tested, **with no coaching from you**
- two improvements you actually made, recorded with a before and after

---

## 1. Wireframes

| # | Screen | File | Status |
|---|---|---|---|
| 1 | Splash | `docs/wireframes/mockup_02_splash_as_drawn.png` | Drawn |
| 2 | Sign in | `docs/wireframes/mockup_01_login_as_drawn.png` | Drawn (as "Login") |
| 3 | Sign up | — | **Needed** |
| 4 | Roster (lecturer) | — | **Needed** |
| 5 | Student editor | — | **Needed** |
| 6 | My profile | — | **Needed** |
| 7 | Sync status | — | **Needed** |

Pen and paper is acceptable and often better — a hand sketch invites criticism
where a polished screen invites approval, and criticism is the point. Photograph
them and drop them in `docs/wireframes/`.

The published prototype renders all seven screens, so screenshots of it are a
legitimate second set showing the *revised* design against the hand wireframes.

---

## 2. Test sessions

Run each tester on the prototype, one task at a time. **Say nothing while they
work.** The urge to help is the thing that invalidates the session.

Protocol:

1. Set the task in the prototype's usability panel and enter their initials.
2. Press **Start**, hand over the device, and stop talking.
3. Press **They finished** or **They gave up** when it resolves.
4. Write down, in their words, anything they said aloud and where they paused.

The panel stores each session with the page, so timings survive and can be read
back when writing this up. The timings are not the finding, though — what they
say and where they hesitate is.

### Tester 1

| | |
|---|---|
| Initials | |
| Programme / year | |
| Date | |
| Device | |

| Task | Styling | Time | Outcome | What they did, said, or got stuck on |
|---|---|---|---|---|
| Register yourself as a new student | | | | |
| Find Mainza and note her lab group | | | | |
| Say which records have not reached the server | | | | |

### Tester 2

| | |
|---|---|
| Initials | |
| Programme / year | |
| Date | |
| Device | |

| Task | Styling | Time | Outcome | What they did, said, or got stuck on |
|---|---|---|---|---|
| Register yourself as a new student | | | | |
| Find Mainza and note her lab group | | | | |
| Say which records have not reached the server | | | | |

### Tester 3

| | |
|---|---|
| Initials | |
| Programme / year | |
| Date | |
| Device | |

| Task | Styling | Time | Outcome | What they did, said, or got stuck on |
|---|---|---|---|---|
| Register yourself as a new student | | | | |
| Find Mainza and note her lab group | | | | |
| Say which records have not reached the server | | | | |

---

## 3. Improvements made

Two, with a before and after. The brief wants changes you *made*, so each one
needs a commit behind it.

### Improvement 1

| | |
|---|---|
| What testers did | |
| How many of the three | |
| What that told you | |
| What you changed | |
| Before | screenshot / wireframe reference |
| After | screenshot / wireframe reference |
| Commit | |

### Improvement 2

| | |
|---|---|
| What testers did | |
| How many of the three | |
| What that told you | |
| What you changed | |
| Before | screenshot / wireframe reference |
| After | screenshot / wireframe reference |
| Commit | |

---

## 4. Changes already made before testing, and why

These are **not** findings — nothing was observed. They are corrections made
from measurement and from the design being self-contradictory, and they are
recorded here so the distinction is visible rather than blurred.

| Change | Basis |
|---|---|
| Field placeholders moved off red-on-blue to dark ink on the surface | The mocked pair measures **1.00 : 1** against WCAG AA's 4.5 : 1. Arithmetic, reproducible — see `design_decisions.md`. |
| The screen titled "Login" split into sign-in and sign-up | The mockup had registration fields and no password box, so as drawn nobody could sign in. A correctness problem. |
| Placeholders replaced with floating labels | The label vanished once a field had content. |
| Palette resampled from the crest | The mockup's blue and red were approximations of the real crest colours. |

If testing contradicts any of these, that is a finding and it goes in section 3.

`design_decisions.md` lists five open questions with the task that checks each.
The prototype carries all five as selectable tasks.
