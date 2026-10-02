# Accessibility Checklist — Ben Chola (202404172)

## Labels
- Every input has android:hint
- All icons have contentDescription

## Touch Targets
- All buttons ≥ 48dp tall
- Minimum 16dp padding between interactive elements

## Text Size
- Layouts work at 200% text size
- ScrollView + wrap_content for flexibility

## TalkBack
- Back button labelled "Navigate up"
- Every button has a clear label

## Errors
- Shown inline below fields
- Preserve typed input after error

## Back Navigation
- Every Activity has an up arrow
- Predictable parent chain
