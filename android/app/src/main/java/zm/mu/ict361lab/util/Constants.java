package zm.mu.ict361lab.util;

public final class Constants {
    private Constants() {}

    public static final String ROLE_LECTURER = "LECTURER";
    public static final String ROLE_STUDENT  = "STUDENT";

    /** The five sync states the brief names, shown to the user verbatim. */
    public static final String LOCAL_SYNCED          = "SYNCED";
    public static final String LOCAL_SAVED_LOCAL     = "SAVED_LOCAL";
    public static final String LOCAL_PENDING         = "PENDING";
    public static final String LOCAL_SYNCING         = "SYNCING";
    public static final String LOCAL_ACTION_REQUIRED = "ACTION_REQUIRED";

    /** Operation types, matching the /sync contract. */
    public static final String OP_CREATE = "CREATE";
    public static final String OP_UPDATE = "UPDATE";
    public static final String OP_ASSIGN = "ASSIGN";
    public static final String OP_DELETE = "DELETE";

    public static final String EXTRA_STUDENT_ID = "extra_student_id";
    public static final int PAGE_SIZE = 10;
}
