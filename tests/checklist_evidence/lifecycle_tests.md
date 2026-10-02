# Abel Simasiku (202407015) — Lifecycle Tests

## Rotation test
Opened Login screen → typed email + password → rotated emulator → form state preserved.
Result: PASS

## Process death test
Opened Register screen → typed claim code + name → killed app (swipe away) → reopened → draft still in Room.
Result: PASS

## TalkBack test
Enabled TalkBack → navigated all screens → every field announced correctly.
Result: PASS
