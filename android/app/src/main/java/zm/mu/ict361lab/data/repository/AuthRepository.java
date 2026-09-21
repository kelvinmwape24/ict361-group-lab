package zm.mu.ict361lab.data.repository;

import android.content.Context;

import java.io.IOException;

import retrofit2.Response;
import zm.mu.ict361lab.data.remote.ApiService;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.data.remote.RetrofitClient;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.AppExecutors;
import zm.mu.ict361lab.util.TokenStore;

/**
 * Sign in and registration.
 *
 * The password is used once, in the request body, and never written anywhere.
 * What we keep is the token, the role and the account id.
 */
public class AuthRepository {

    private final ApiService api;
    private final TokenStore tokens;
    private final AppExecutors executors = AppExecutors.get();

    public AuthRepository(Context context) {
        this.api = RetrofitClient.get(context);
        this.tokens = new TokenStore(context);
    }

    public void login(String email, String password, Result.Callback<Dtos.AuthResponse> callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<Dtos.AuthResponse> response =
                        api.login(new Dtos.LoginRequest(email.trim(), password)).execute();
                if (!response.isSuccessful()) {
                    deliver(callback, Result.failure(ApiErrors.parse(response).error));
                    return;
                }
                store(response.body(), email.trim());
                deliver(callback, Result.success(response.body()));
            } catch (IOException e) {
                deliver(callback, Result.offline());
            }
        });
    }

    public void register(String claimCode, String studentNumber, String name, String password,
                         Result.Callback<Dtos.AuthResponse> callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<Dtos.AuthResponse> response = api.register(
                        new Dtos.RegisterRequest(claimCode.trim(), studentNumber.trim(),
                                name.trim(), password)).execute();
                if (!response.isSuccessful()) {
                    deliver(callback, Result.failure(ApiErrors.parse(response).error));
                    return;
                }
                store(response.body(), studentNumber.trim() + "@student.mu");
                tokens.clearDraft();
                deliver(callback, Result.success(response.body()));
            } catch (IOException e) {
                // Account verification has to happen online. The form itself is
                // kept as a draft by the screen so nothing typed is lost.
                deliver(callback, Result.offline());
            }
        });
    }

    private void store(Dtos.AuthResponse auth, String identity) {
        if (auth == null) return;
        // The account id is not in the token payload we can read here, so the
        // student id (or the lecturer's email) scopes local data instead —
        // either way, one signed-in identity, one local namespace.
        String scope = auth.student_id != null ? auth.student_id : identity;
        tokens.save(auth.token, auth.role, auth.student_id, scope);
    }

    private <T> void deliver(Result.Callback<T> callback, Result<T> result) {
        if (callback == null) return;
        executors.mainThread().execute(() -> callback.onResult(result));
    }
}
