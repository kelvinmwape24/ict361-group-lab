package zm.mu.ict361lab.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import zm.mu.ict361lab.data.remote.dto.LoginResponse;
import zm.mu.ict361lab.data.repository.AuthRepository;
import zm.mu.ict361lab.ui.common.UiState;

public class LoginViewModel extends ViewModel {
    private final AuthRepository repo = new AuthRepository();
    private final MutableLiveData<UiState<LoginResponse>> state = new MutableLiveData<>();

    public LiveData<UiState<LoginResponse>> getState() { return state; }

    public void login(String email, String password) {
        state.setValue(UiState.loading());
        repo.login(email, password, new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> c, Response<LoginResponse> r) {
                if (r.isSuccessful() && r.body() != null)
                    state.setValue(UiState.success(r.body()));
                else
                    state.setValue(UiState.error("Invalid credentials"));
            }
            @Override
            public void onFailure(Call<LoginResponse> c, Throwable t) {
                state.setValue(UiState.error("Network error"));
            }
        });
    }
}
