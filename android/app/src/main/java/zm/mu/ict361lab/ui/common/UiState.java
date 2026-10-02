package zm.mu.ict361lab.ui.common;

public class UiState<T> {
    public enum Status { LOADING, SUCCESS, ERROR, OFFLINE }
    public final Status status;
    public final T data;
    public final String message;

    private UiState(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> UiState<T> loading() {
        return new UiState<>(Status.LOADING, null, null);
    }
    public static <T> UiState<T> success(T data) {
        return new UiState<>(Status.SUCCESS, data, null);
    }
    public static <T> UiState<T> error(String message) {
        return new UiState<>(Status.ERROR, null, message);
    }
    public static <T> UiState<T> offline(T data) {
        return new UiState<>(Status.OFFLINE, data, "Showing cached");
    }
}
