package LinkedLibrary;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/** Tests for HoldRequest. */
public class HoldRequestTest
{
    private Patron ana;
    private HoldRequest hold;

    @Before
    public void setUp()
    {
        ana = new Patron("Ana");
        hold = new HoldRequest(ana, " The Hobbit ", 3);
    }


    @Test
    public void gettersReturnTrimmedValues()
    {
        assertSame(ana, hold.getPatron());
        assertEquals("The Hobbit", hold.getTitle());
        assertEquals(3, hold.getSequence());
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullPatron()
    {
        new HoldRequest(null, "The Hobbit", 1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsBlankTitle()
    {
        new HoldRequest(ana, "  ", 1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsBadSequence()
    {
        new HoldRequest(ana, "The Hobbit", 0);
    }


    @Test
    public void isForMatchesTitleIgnoringCase()
    {
        assertTrue(hold.isFor("the hobbit"));
    }


    @Test
    public void isForRejectsOtherNullAndBlank()
    {
        assertFalse(hold.isFor("Dune"));
        assertFalse(hold.isFor(null));
        assertFalse(hold.isFor(""));
    }


    @Test
    public void samePatronSameTitleIsDuplicate()
    {
        HoldRequest later = new HoldRequest(new Patron("Ana"), "THE HOBBIT", 9);
        assertEquals(hold, later);
        assertEquals(hold.hashCode(), later.hashCode());
    }


    @Test
    public void differentPatronOrTitleIsNotDuplicate()
    {
        assertNotEquals(
            hold,
            new HoldRequest(new Patron("Bo"), "The Hobbit", 4));
        assertNotEquals(hold, new HoldRequest(ana, "Dune", 4));
        assertFalse(hold.equals(null));
        assertFalse(hold.equals("The Hobbit"));
    }


    @Test
    public void toStringShowsWhoAndWhat()
    {
        assertEquals("Ana waiting for The Hobbit", hold.toString());
    }
}
