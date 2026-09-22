# Design decisions — the mockups, and what changed

The two mockups in `docs/wireframes/` are the starting point. This records what
was carried over, what was changed, and — separately and explicitly — what is
**measured** as against what is still an **open question for Activity A
testing**.

That split matters. A design note that presents an untested opinion as a
finding is worth nothing under questioning, and the first person to ask "who
did you test that with?" will find out.

---

## Carried over unchanged

| From the mockup | Where it now lives |
|---|---|
| Solid blue splash with the crest centred | `Theme.ICT361Lab.Splash`, `drawable/splash_background.xml`, and `values-v31/themes.xml` for Android 12+ |
| Crest and "MULUNGUSHI UNIVERSITY / SCHOOL OF ENGINEERING AND TECHNOLOGY" lock-up at the top of the auth screens | `layout/include_brand_header.xml`, included by both auth screens |
| Centred screen title with a one-line subtitle beneath | `TextAppearance.ICT361.ScreenTitle` / `.ScreenSubtitle` |
| Full-width stacked fields, generous vertical rhythm | `Widget.ICT361.Field` |
| A bordered pill for the secondary action | `Widget.ICT361.Button.Outlined` |
| The blue, and the red | Sampled from the crest itself — see below |

---

## Measured: the colours

The mockup's blues and reds were approximations. The palette in
`res/values/colors.xml` is sampled from `docs/brand/mu_crest_source.png` — the
actual crest — and then checked for contrast.

| Token | Value | On white | Verdict |
|---|---|---|---|
| `mu_blue` | `#4E52B3` | 6.60 : 1 | Passes AA at any size, AAA at large |
| `mu_blue_dark` | `#3A3E8F` | 9.27 : 1 | Passes AAA |
| `mu_red` | `#D23B30` | 4.72 : 1 | Passes AA for body text — used for fills |
| `mu_red_text` | `#B3281F` | 6.78 : 1 | Used for error text, which must not sit at the margin |

`colorError` is `mu_red_text`, not `mu_red`. Error messages are the text a user
reads under pressure; 4.72 : 1 is a pass but not a comfortable one.

### Measured: the field styling in the mockup does not work

The mockup's fields are a blue fill (`#4A7BC8`) with red placeholder text
(`#E53935`) and no label.

**That combination measures 1.00 : 1.** Not "low contrast" — the two colours
have effectively identical relative luminance, so the ratio is the floor. WCAG
AA requires 4.5 : 1 for body text. The text is invisible to the algorithm and
close to invisible on a phone in daylight.

This is arithmetic, not opinion, and it is reproducible: compute the WCAG
relative luminance of each colour and take the ratio. The published prototype
shows both treatments side by side with the live figure, so it can be
demonstrated rather than asserted.

Placeholders were also the only labels. Once a field has content the label is
gone, so a user who looks away cannot tell which box is which — and neither can
anyone reviewing a filled-in form. The revised fields use Material's outlined
box with a floating hint: the label rises and stays.

**Revised:** ink `#14161D` on the surface — 14.6 : 1.

---

## Changed: the screen titled "Login" was a registration form

The mockup's screen reads **Login / Sign in to continue**, and then asks for
student number, name, phone number and email, with a **sign up** button and no
password field.

Those are two different screens' worth of content. Whatever the intent, the
screen cannot do both: signing in needs a credential, and registering needs
details. So:

- **`activity_login.xml`** — sign in. Student number or username, password, a
  filled "Sign in", and an outlined "Create one" leading to sign-up.
- **`activity_register.xml`** — sign up. The mockup's layout and its "sign up"
  wording, plus an outlined route back to sign-in, which the mockup had no way
  of reaching.

This is a correctness change, not a preference: there is no password box on the
mockup, so as drawn nobody can log in.

---

## Raised with the team, not changed unilaterally

**Phone number and email on the registration form.** The mockup asks for both.
Neither exists on `students`, so adding them touches:

- `database/migrations/` — a new additive migration
- `server/util/validate.js`, `server/routes/students.js`, `server/routes/me.js` — Kelvin's
- `data/local/entity/LocalStudentEntity.java` and a Room migration — Collins's
- `data/remote/Dtos.java`, the two auth layouts, `AuthViewModel` — this area's

That is three people's files and a schema version, which is a team decision
rather than one to make quietly in a branch. Flagged to Kelvin and Collins.

The **web prototype shows both fields in place**, so the design can be reviewed
and tested before anyone commits to the schema. If the team says yes, the
migration is small and additive; if it says no, the mockup drops two fields and
nothing else changes.

---

## Open questions — for Activity A testing, not yet answered

Nothing below has been tested. These are hypotheses with a way to check each
one, which is what the testing session is for. Record the answers in
`usability_findings.md` as they come in.

1. **Does anyone find the "sign up" button?** A secondary action under a filled
   primary is easy to skip. Task: *"Register yourself as a new student"*, given
   to someone who starts on the sign-in screen.
2. **Do the mocked fields actually stop people?** The contrast figure predicts
   they will. Task: the same registration task, run once against each styling,
   timing both. The prototype's A/B switch and session recorder exist for this.
3. **Is "Student number or username" clear**, or does a student try their email?
4. **Does anyone understand the five sync labels?** `SAVED_LOCAL` and `PENDING`
   are a distinction the app makes and a user might not. Task: *"Say which
   records have not yet reached the server."*
5. **Is a full group legible before it is tapped?** The roster shows `G01
   (14/15)` in the filter; the editor shows a capacity bar. Task: *"Join lab
   group G01"* when it is already full.

The published prototype carries all five as selectable tasks, times each
session and stores the result, so the findings come from recorded sessions
rather than from memory afterwards.
