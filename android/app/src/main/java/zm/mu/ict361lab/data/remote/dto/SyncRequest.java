package zm.mu.ict361lab.data.remote.dto;
public class SyncRequest {
    public String operation_id;
    public String type;
    public Object payload;
    public int base_version;
}
