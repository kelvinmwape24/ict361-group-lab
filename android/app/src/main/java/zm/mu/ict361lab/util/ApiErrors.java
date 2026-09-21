package zm.mu.ict361lab.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import retrofit2.Response;
import zm.mu.ict361lab.R;
import zm.mu.ict361lab.data.remote.Dtos;

/**
 * Turns a server error code into something a person can act on.
 *
 * The server sends machine codes. The user gets a sentence that says what went
 * wrong and what to do — never a raw code, never an apology, never "an error
 * occurred".
 */
public final class ApiErrors {

    private static final Gson GSON = new Gson();

    private ApiErrors() {}

    public static Dtos.ErrorDto parse(Response<?> response) {
        Dtos.ErrorDto dto = new Dtos.ErrorDto();
        try {
            if (response.errorBody() != null) {
                Dtos.ErrorDto parsed = GSON.fromJson(response.errorBody().string(), Dtos.ErrorDto.class);
                if (parsed != null && parsed.error != null) return parsed;
            }
        } catch (IOException | JsonSyntaxException ignored) {
            // fall through to the generic code below
        }
        dto.error = "HTTP_" + response.code();
        return dto;
    }

    /** Maps a server code onto a string resource. */
    public static int messageFor(String code) {
        if (code == null) return R.string.err_server;
        switch (code) {
            case "INVALID_CREDENTIALS":   return R.string.err_invalid_credentials;
            case "CLAIM_CODE_NOT_FOUND":  return R.string.err_claim_not_found;
            case "ALREADY_REGISTERED":    return R.string.err_already_registered;
            case "DUPLICATE_NUMBER":      return R.string.err_duplicate_number;
            case "GROUP_FULL":            return R.string.err_group_full;
            case "CONFLICT":              return R.string.err_conflict;
            case "STUDENT_DELETED":       return R.string.err_student_deleted;
            case "REQUEST_ALREADY_PENDING": return R.string.err_request_pending;
            case "FORBIDDEN":             return R.string.err_forbidden;
            case "INVALID_NAME":          return R.string.err_name_length;
            case "INVALID_STUDENT_NUMBER":return R.string.err_student_number;
            case "WEAK_PASSWORD":         return R.string.err_password_short;
            case "NO_TOKEN":
            case "INVALID_TOKEN":
            case "SESSION_REVOKED":       return R.string.err_session_expired;
            default:                      return R.string.err_server;
        }
    }

    /** A revoked or expired session means stop syncing and sign in again. */
    public static boolean isSessionGone(String code) {
        return "NO_TOKEN".equals(code) || "INVALID_TOKEN".equals(code)
                || "SESSION_REVOKED".equals(code);
    }
}
