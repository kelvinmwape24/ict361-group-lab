package zm.mu.ict361lab.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import zm.mu.ict361lab.data.remote.Dtos;

/**
 * Programmes come from stored reference data on the server, not from a constant
 * in the app.
 *
 * The brief says the programme is chosen "from stored reference data". Hard-coding
 * CS/IT/DS would mean a new APK every time the university adds a programme, and
 * would let the app offer a programme the database has never heard of.
 *
 * The list is cached here so the picker still works offline. If the cache is
 * empty — a fresh install that has never been online — the caller disables the
 * programme field and says so, rather than inventing options. program_id is
 * nullable in the schema, so a student can be created now and given a
 * programme later.
 */
public final class ProgrammeStore {

    private static final String FILE = "ict361_reference";
    private static final String KEY = "programmes";
    private static final Gson GSON = new Gson();

    private final SharedPreferences prefs;

    public ProgrammeStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public void save(List<Dtos.ProgrammeDto> programmes) {
        if (programmes == null || programmes.isEmpty()) return;
        prefs.edit().putString(KEY, GSON.toJson(programmes)).apply();
    }

    public List<Dtos.ProgrammeDto> all() {
        String json = prefs.getString(KEY, null);
        if (json == null) return new ArrayList<>();
        try {
            List<Dtos.ProgrammeDto> list = GSON.fromJson(json,
                    new TypeToken<List<Dtos.ProgrammeDto>>() {}.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public boolean isEmpty() { return all().isEmpty(); }

    public List<String> codes() {
        List<String> codes = new ArrayList<>();
        for (Dtos.ProgrammeDto p : all()) codes.add(p.code);
        return codes;
    }

    public Integer idFor(String code) {
        if (code == null) return null;
        for (Dtos.ProgrammeDto p : all()) if (code.equals(p.code)) return p.program_id;
        return null;
    }

    public String codeFor(Integer id) {
        if (id == null) return null;
        for (Dtos.ProgrammeDto p : all()) if (p.program_id == id) return p.code;
        return null;
    }

    /** Spinner position of a code, or 0 when it is not in the list. */
    public int positionOf(String code) {
        List<String> codes = codes();
        int index = codes.indexOf(code);
        return index < 0 ? 0 : index;
    }

    public void clear() { prefs.edit().remove(KEY).apply(); }
}
