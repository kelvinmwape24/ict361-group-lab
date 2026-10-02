package zm.mu.ict361lab;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class FakeRepoTest {
    private static class FakeRepo {
        public String getName() { return "Kelvin Mwape"; }
    }

    @Test
    public void testFakeRepoReturnsName() {
        assertEquals("Kelvin Mwape", new FakeRepo().getName());
    }
}
