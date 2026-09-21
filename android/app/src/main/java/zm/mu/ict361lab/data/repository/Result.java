package zm.mu.ict361lab.data.repository;

/**
 * What a repository call answers with. Three outcomes, not two: succeeded,
 * failed with a reason the server gave, or could not reach the server at all —
 * and the third is a normal state in this app, not an error.
 */
public final class Result<T> {

    public final boolean ok;
    public final T data;
    public final String errorCode;
    public final boolean offline;
    public final Integer serverVersion;

    private Result(boolean ok, T data, String errorCode, boolean offline, Integer serverVersion) {
        this.ok = ok;
        this.data = data;
        this.errorCode = errorCode;
        this.offline = offline;
        this.serverVersion = serverVersion;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, false, null);
    }

    public static <T> Result<T> failure(String code) {
        return new Result<>(false, null, code, false, null);
    }

    public static <T> Result<T> conflict(String code, Integer serverVersion) {
        return new Result<>(false, null, code, false, serverVersion);
    }

    /** Reached no server. The work is saved locally and will be retried. */
    public static <T> Result<T> offline() {
        return new Result<>(false, null, "OFFLINE", true, null);
    }

    public interface Callback<T> {
        void onResult(Result<T> result);
    }
}
