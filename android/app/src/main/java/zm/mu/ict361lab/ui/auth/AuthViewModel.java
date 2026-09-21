package zm.mu.ict361lab.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.data.repository.AuthRepository;
import zm.mu.ict361lab.ui.common.UiState;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<UiState<Dtos.AuthResponse>> state = new MutableLiveData<>();

    public AuthViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<UiState<Dtos.AuthResponse>> state() { return state; }

    public void signIn(String email, String password) {
        state.setValue(UiState.loading());
        repository.login(email, password, result -> {
            if (result.ok)            state.setValue(UiState.success(result.data));
            else if (result.offline)  state.setValue(UiState.offline());
            else                      state.setValue(UiState.error(result.errorCode));
        });
    }

    public void register(String claimCode, String number, String name, String password) {
        state.setValue(UiState.loading());
        repository.register(claimCode, number, name, password, result -> {
            if (result.ok)            state.setValue(UiState.success(result.data));
            else if (result.offline)  state.setValue(UiState.offline());
            else                      state.setValue(UiState.error(result.errorCode));
        });
    }

    /** Lets the screen clear a handled state so rotation does not re-fire it. */
    public void consume() { state.setValue(null); }
}
