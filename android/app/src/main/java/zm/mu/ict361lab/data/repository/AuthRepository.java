package zm.mu.ict361lab.data.repository;

import android.content.Context;

import java.io.IOException;

import retrofit2.Response;
import zm.mu.ict361lab.data.local.AppDatabase;
import zm.mu.ict361lab.data.local.entity.AccountEntity;
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

    private final Context appContext;
    private final ApiService api;
    private final TokenStore tokens;
    private final AppExecutors executors = AppExecutors.get();

    public AuthRepository(Context context) {
        this.appContext = context.getApplicationContext();
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
                deliver(callback, Result.offline());
            }
        });
    }

    private void store(Dtos.AuthResponse auth, String identity) {
        if (auth == null) return;
        String scope = auth.student_id != null ? auth.student_id : identity;
        tokens.save(auth.token, auth.role, auth.student_id, scope);
        rememberAccount(scope, auth.role, identity);
    }

    /**
     * Caches this account locally so the device knows who has signed in here
     * before, even while offline. Runs on diskIO — the same single thread
     * every other Room write in this app uses, so writes stay ordered.
     */
    private void rememberAccount(String accountId, String role, String identity) {
        AccountEntity account = new AccountEntity();
        account.accountId = accountId;
        account.role = role;
        account.identity = identity;
        account.createdAt = System.currentTimeMillis();
        executors.diskIO().execute(() ->
                AppDatabase.get(appContext).accountDao().upsert(account));
    }

    private <T> void deliver(Result.Callback<T> callback, Result<T> result) {
        if (callback == null) return;
        executors.mainThread().execute(() -> callback.onResult(result));
    }
}