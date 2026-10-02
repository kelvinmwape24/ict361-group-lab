package zm.mu.ict361lab.data.repository;

import retrofit2.Callback;
import zm.mu.ict361lab.data.remote.ApiService;
import zm.mu.ict361lab.data.remote.RetrofitClient;
import zm.mu.ict361lab.data.remote.dto.AssignRequest;
import zm.mu.ict361lab.data.remote.dto.StudentDto;

public class StudentRepository {
    private final ApiService api = RetrofitClient.api();

    public void listStudents(String token, String q, String group, int page, int size,
                             Callback<java.util.List<StudentDto>> cb) {
        api.listStudents("Bearer " + token, q, null, group, page, size).enqueue(cb);
    }

    public void assignGroup(String token, String studentId, int groupId, Callback<Void> cb) {
        api.assignGroup("Bearer " + token, studentId, new AssignRequest(groupId)).enqueue(cb);
    }
}
