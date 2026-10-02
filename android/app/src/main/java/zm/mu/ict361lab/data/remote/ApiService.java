package zm.mu.ict361lab.data.remote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;
import zm.mu.ict361lab.data.remote.dto.*;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest req);

    @POST("auth/register")
    Call<LoginResponse> register(@Body RegisterRequest req);

    @GET("students")
    Call<List<StudentDto>> listStudents(
        @Header("Authorization") String token,
        @Query("q") String q,
        @Query("program") String program,
        @Query("group") String group,
        @Query("page") int page,
        @Query("size") int size
    );

    @GET("students/me")
    Call<StudentDto> getMyProfile(@Header("Authorization") String token);

    @POST("students/{id}/assign")
    Call<Void> assignGroup(
        @Header("Authorization") String token,
        @Path("id") String studentId,
        @Body AssignRequest req
    );

    @POST("sync")
    Call<SyncResponse> sync(
        @Header("Authorization") String token,
        @Body SyncRequest req
    );
}
