package zm.mu.ict361lab.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * The only way this app talks to the backend. Android never opens a MySQL
 * connection; it calls these routes over HTTP, and the Authorization header is
 * attached by an OkHttp interceptor rather than by every call site.
 */
public interface ApiService {

    /* auth */
    @POST("auth/login")
    Call<Dtos.AuthResponse> login(@Body Dtos.LoginRequest body);

    @POST("auth/register")
    Call<Dtos.AuthResponse> register(@Body Dtos.RegisterRequest body);

    /* lecturer */
    @GET("students")
    Call<Dtos.PageDto> students(
            @Query("q") String query,
            @Query("program") String programme,
            @Query("group") String group,
            @Query("page") int page,
            @Query("size") int size);

    @POST("students")
    Call<Dtos.CreateStudentResponse> createStudent(@Body Dtos.CreateStudentRequest body);

    @PATCH("students/{id}")
    Call<Dtos.SimpleResponse> patchStudent(@Path("id") String id,
                                           @Body Dtos.PatchStudentRequest body);

    @DELETE("students/{id}")
    Call<Dtos.SimpleResponse> deleteStudent(@Path("id") String id);

    @POST("students/{id}/assign")
    Call<Dtos.SimpleResponse> assign(@Path("id") String id, @Body Dtos.AssignRequest body);

    /* student */
    @GET("students/me")
    Call<Dtos.MeDto> me();

    @PATCH("students/me")
    Call<Dtos.SimpleResponse> patchMe(@Body Dtos.PatchStudentRequest body);

    @POST("students/me/group-request")
    Call<Dtos.SimpleResponse> requestGroup(@Body Dtos.AssignRequest body);

    @POST("students/me/number-correction")
    Call<Dtos.SimpleResponse> requestNumberCorrection(@Body Dtos.NumberCorrectionRequest body);

    /* groups — label and counts only, which is all the Sharesheet summary may show */
    @GET("groups")
    Call<Dtos.GroupsResponse> groups();

    /* programmes — stored reference data, so the picker is never hard-coded */
    @GET("programmes")
    Call<Dtos.ProgrammesResponse> programmes();

    /* sync */
    @POST("sync")
    Call<Dtos.SyncResponse> push(@Body Dtos.SyncRequest body);

    @GET("sync/changes")
    Call<Dtos.ChangesResponse> changes(@Query("since") String since);
}
