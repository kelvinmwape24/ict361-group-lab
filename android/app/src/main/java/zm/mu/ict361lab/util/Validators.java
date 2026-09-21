package zm.mu.ict361lab.util;

import java.util.regex.Pattern;

/**
 * The same rules the server enforces, checked locally so the user gets an
 * answer immediately. The server still validates everything — this is for
 * feedback, never for trust.
 *
 * Kept deliberately in step with server/util/validate.js. If one changes, both
 * change: a client that accepts what the server rejects produces an error the
 * user cannot explain.
 */
public final class Validators {

    /**
     * Ordinary name punctuation and accented letters are allowed, as the brief
     * requires. \p{L} is any Unicode letter and \p{M} any combining mark, so
     * Bwalya, Traoré, O'Brien-Phiri and Agrippa C. Hamasukwa all pass.
     *
     * Digits do not. A name is not a number, and admitting "12345" is how a
     * roster becomes unsearchable.
     */
    private static final Pattern NAME =
            Pattern.compile("^[\\p{L}\\p{M}][\\p{L}\\p{M} '.\\-]*$");

    /** Exactly nine digits. A String, never an int, so a leading zero survives. */
    private static final Pattern STUDENT_NUMBER = Pattern.compile("^\\d{9}$");

    private Validators() {}

    public static boolean name(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        if (trimmed.length() < 2 || trimmed.length() > 100) return false;
        return NAME.matcher(trimmed).matches();
    }

    /**
     * Outer whitespace is trimmed. An embedded space is rejected rather than
     * silently stripped — "2022 03897" is a typo the user should see, not a
     * value for us to guess at.
     */
    public static boolean studentNumber(String value) {
        if (value == null) return false;
        return STUDENT_NUMBER.matcher(value.trim()).matches();
    }

    public static boolean password(String value) {
        return value != null && value.length() >= 6;
    }

    public static boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
