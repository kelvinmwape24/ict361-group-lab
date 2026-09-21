package zm.mu.ict361lab.data.remote;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import zm.mu.ict361lab.BuildConfig;
import zm.mu.ict361lab.util.TokenStore;

public final class RetrofitClient {

    private static volatile ApiService service;

    private RetrofitClient() {}

    public static ApiService get(Context context) {
        if (service == null) {
            synchronized (RetrofitClient.class) {
                if (service == null) {
                    service = build(context.getApplicationContext());
                }
            }
        }
        return service;
    }

    private static ApiService build(Context appContext) {
        final TokenStore tokens = new TokenStore(appContext);

        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            String header = tokens.authHeader();
            if (header == null) return chain.proceed(original);
            return chain.proceed(original.newBuilder()
                    .header("Authorization", header)
                    .build());
        };

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // BASIC, never BODY: the login request body carries a password, and a
        // password in logcat is a password on the marker's screen.
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }
}
