package zm.mu.ict361lab;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import zm.mu.ict361lab.util.Validators;

/**
 * The validation rules from the brief's "Data validation and integrity"
 * section, checked against the cases that actually caused trouble.
 *
 * These must agree with server/util/validate.js. A client that accepts what the
 * server rejects produces an error the user cannot explain.
 */
public class ValidatorsTest {

    @Test
    public void acceptsOrdinaryZambianAndAccentedNames() {
        assertTrue(Validators.name("Kelvin Mwape"));
        assertTrue(Validators.name("Kansamba Auxiria"));
        assertTrue("accented letters are explicitly allowed",
                Validators.name("Mordecai Salim Traoré"));
        assertTrue("an initial with a full stop is ordinary name punctuation",
                Validators.name("Agrippa C. Hamasukwa"));
        assertTrue("apostrophes and hyphens too", Validators.name("O'Brien-Phiri"));
        assertTrue("two characters is the floor, and it is inclusive",
                Validators.name("Bo"));
    }

    @Test
    public void rejectsNamesThatAreNotNames() {
        assertFalse("one character is below the floor", Validators.name("A"));
        assertFalse("a number is not a name", Validators.name("12345"));
        assertFalse("nor a name with digits in it", Validators.name("Bob2"));
        assertFalse(Validators.name(""));
        assertFalse(Validators.name("   "));
        assertFalse(Validators.name(null));

        StringBuilder tooLong = new StringBuilder();
        for (int i = 0; i < 101; i++) tooLong.append('a');
        assertFalse("101 characters is over the ceiling", Validators.name(tooLong.toString()));
    }

    @Test
    public void nameLengthIsMeasuredAfterTrimming() {
        assertTrue(Validators.name("  Salima Banda  "));
        assertFalse("surrounding spaces do not pad a one-character name",
                Validators.name("  A  "));
    }

    @Test
    public void studentNumberIsNineDigitsAndKeepsLeadingZeroes() {
        assertTrue(Validators.studentNumber("202203897"));
        assertTrue("a leading zero is significant, which is why this is a String",
                Validators.studentNumber("000000001"));
        assertTrue("outer whitespace is trimmed", Validators.studentNumber("  202203897  "));
    }

    @Test
    public void studentNumberRejectsEverythingElse() {
        assertFalse("eight digits", Validators.studentNumber("20220389"));
        assertFalse("ten digits", Validators.studentNumber("2022038971"));
        assertFalse("an embedded space is a typo the user should see, not one we strip",
                Validators.studentNumber("2022 03897"));
        assertFalse("letters", Validators.studentNumber("20220389a"));
        assertFalse(Validators.studentNumber(""));
        assertFalse(Validators.studentNumber(null));
    }

    @Test
    public void passwordFloorMatchesTheServer() {
        assertTrue(Validators.password("secret123"));
        assertTrue(Validators.password("abcdef"));
        assertFalse(Validators.password("abcde"));
        assertFalse(Validators.password(null));
    }
}
