package zm.mu.ict361lab.data.repository;

import retrofit2.Callback;
import zm.mu.ict361lab.data.remote.ApiService;
import zm.mu.ict361lab.data.remote.RetrofitClient;
import zm.mu.ict361lab.data.remote.dto.*;

public class AuthRepository {
    private final ApiService api = RetrofitClient.api();

    public void login(String email, String password, Callback<LoginResponse> cb) {
        api.login(new LoginRequest(email, password)).enqueue(cb);
    }

    public void register(RegisterRequest req, Callback<LoginResponse> cb) {
        api.register(req).enqueue(cb);
    }
}
