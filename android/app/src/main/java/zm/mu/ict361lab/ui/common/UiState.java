package zm.mu.ict361lab.ui.common;

/**
 * What a screen is currently showing. Loading, empty, error and offline are all
 * states the user will actually hit, so they are all modelled — not left as an
 * afterthought behind a spinner that never goes away.
 */
public final class UiState<T> {

    public final boolean loading;
    public final T data;
    public final String errorCode;
    public final boolean offline;

    private UiState(boolean loading, T data, String errorCode, boolean offline) {
        this.loading = loading;
        this.data = data;
        this.errorCode = errorCode;
        this.offline = offline;
    }

    public static <T> UiState<T> loading()          { return new UiState<>(true, null, null, false); }
    public static <T> UiState<T> success(T data)    { return new UiState<>(false, data, null, false); }
    public static <T> UiState<T> error(String code) { return new UiState<>(false, null, code, false); }
    public static <T> UiState<T> offline()          { return new UiState<>(false, null, "OFFLINE", true); }

    public boolean isError() { return errorCode != null; }
}
