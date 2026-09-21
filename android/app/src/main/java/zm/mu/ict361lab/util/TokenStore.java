package zm.mu.ict361lab.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Holds the session token, the role and the signed-in account.
 *
 * The password is NEVER stored — the brief is explicit about that. On sign-out
 * everything here is cleared, and the caller also clears this account's rows
 * and queue so nothing leaks into the next session.
 */
public final class TokenStore {

    private static final String FILE = "ict361_session";
    private static final String K_TOKEN   = "token";
    private static final String K_ROLE    = "role";
    private static final String K_STUDENT = "student_id";
    private static final String K_ACCOUNT = "account_id";
    private static final String K_SINCE   = "last_sync";
    private static final String K_DRAFT   = "registration_draft";

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public void save(String token, String role, String studentId, String accountId) {
        prefs.edit()
                .putString(K_TOKEN, token)
                .putString(K_ROLE, role)
                .putString(K_STUDENT, studentId)
                .putString(K_ACCOUNT, accountId)
                .apply();
    }

    public String token()     { return prefs.getString(K_TOKEN, null); }
    public String role()      { return prefs.getString(K_ROLE, null); }
    public String studentId() { return prefs.getString(K_STUDENT, null); }

    /**
     * The key every local row and every queued operation is scoped by. Using
     * the account id means signing in as someone else cannot see, or replay,
     * the previous user's queue.
     */
    public String accountId() {
        String id = prefs.getString(K_ACCOUNT, null);
        return id == null ? "anonymous" : id;
    }

    public boolean signedIn()  { return token() != null; }
    public boolean isLecturer(){ return Constants.ROLE_LECTURER.equals(role()); }

    public String authHeader() {
        String t = token();
        return t == null ? null : "Bearer " + t;
    }

    public String lastSync()              { return prefs.getString(K_SINCE, null); }
    public void setLastSync(String iso)   { prefs.edit().putString(K_SINCE, iso).apply(); }

    /** Registration draft, so a form filled offline is not lost. */
    public String draft()                 { return prefs.getString(K_DRAFT, null); }
    public void saveDraft(String json)    { prefs.edit().putString(K_DRAFT, json).apply(); }
    public void clearDraft()              { prefs.edit().remove(K_DRAFT).apply(); }

    public void clearSession() {
        prefs.edit()
                .remove(K_TOKEN).remove(K_ROLE)
                .remove(K_STUDENT).remove(K_ACCOUNT)
                .remove(K_SINCE)
                .apply();
    }
}
