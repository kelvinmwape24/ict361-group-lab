package zm.mu.ict361lab;

import org.junit.Test;
import static org.junit.Assert.*;

public class ViewModelTest {

    private static class FakeAuthRepo {
        public String authenticate(String user, String pass) {
            return pass.equals("admin123") ? "token-xyz" : null;
        }
    }

    @Test
    public void testValidLoginReturnsToken() {
        FakeAuthRepo repo = new FakeAuthRepo();
        assertEquals("token-xyz", repo.authenticate("lecturer", "admin123"));
    }

    @Test
    public void testInvalidLoginReturnsNull() {
        FakeAuthRepo repo = new FakeAuthRepo();
        assertNull(repo.authenticate("lecturer", "wrong"));
    }

    @Test
    public void testStudentNumberLength() {
        String validNumber = "202407015";
        assertEquals(9, validNumber.length());
    }
}
