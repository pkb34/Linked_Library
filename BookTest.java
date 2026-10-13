package LinkedLibrary;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/** Tests for Book: one normal and one bad-input case per public method. */
public class BookTest
{
    private Book hobbit;

    @Before
    public void setUp()
    {
        hobbit = new Book("The Hobbit", "Tolkien", 1);
    }


    @Test
    public void constructorTrimsText()
    {
        Book b = new Book("  Dune ", " Herbert", 2);
        assertEquals("Dune", b.getTitle());
        assertEquals("Herbert", b.getAuthor());
        assertEquals(2, b.getCopyNumber());
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsBlankTitle()
    {
        new Book("   ", "Tolkien", 1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullAuthor()
    {
        new Book("The Hobbit", null, 1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsZeroCopyId()
    {
        new Book("The Hobbit", "Tolkien", 0);
    }


    @Test
    public void dueDayStartsAtZeroAndCanBeSet()
    {
        assertEquals(0, hobbit.getDueDay());
        hobbit.setDueDay(15);
        assertEquals(15, hobbit.getDueDay());
        hobbit.setDueDay(0);
        assertEquals(0, hobbit.getDueDay());
    }


    @Test(expected = IllegalArgumentException.class)
    public void setDueDayRejectsNegative()
    {
        hobbit.setDueDay(-1);
    }


    @Test
    public void daysLateCountsDaysPastDue()
    {
        hobbit.setDueDay(15);
        assertEquals(5, hobbit.daysLate(20));
    }


    @Test
    public void daysLateZeroWhenOnTimeOrOnShelf()
    {
        assertEquals(0, hobbit.daysLate(100));   // not checked out
        hobbit.setDueDay(15);
        assertEquals(0, hobbit.daysLate(15));    // due today
        assertEquals(0, hobbit.daysLate(-5));
    }


    @Test
    public void isBlank()
    {
        assertTrue(Book.isBlank(null));
        assertTrue(Book.isBlank(" \t "));
        assertFalse(Book.isBlank("x"));
    }


    @Test
    public void hasTitleIgnoresCaseAndSpaces()
    {
        assertTrue(hobbit.hasTitle("the hobbit"));
        assertTrue(hobbit.hasTitle("  The Hobbit "));
    }


    @Test
    public void hasTitleRejectsOtherNullAndBlank()
    {
        assertFalse(hobbit.hasTitle("Dune"));
        assertFalse(hobbit.hasTitle("Hobbit"));
        assertFalse(hobbit.hasTitle(null));
        assertFalse(hobbit.hasTitle(" "));
    }


    @Test
    public void matchesTitleOrAuthorIgnoringCase()
    {
        assertTrue(hobbit.matches("hob"));
        assertTrue(hobbit.matches("TOLK"));
        assertFalse(hobbit.matches("Dune"));
    }


    @Test
    public void matchesRejectsNullAndBlank()
    {
        assertFalse(hobbit.matches(null));
        assertFalse(hobbit.matches("   "));
    }


    @Test
    public void equalsUsesCopyIdOnly()
    {
        Book sameCopy = new Book("Other", "Other", 1);
        Book secondCopy = new Book("The Hobbit", "Tolkien", 2);
        assertEquals(hobbit, sameCopy);
        assertEquals(hobbit.hashCode(), sameCopy.hashCode());
        assertNotEquals(hobbit, secondCopy);
    }


    @Test
    public void equalsRejectsNullAndOtherTypes()
    {
        assertFalse(hobbit.equals(null));
        assertFalse(hobbit.equals("The Hobbit"));
    }


    @Test
    public void toStringShowsCopyTitleAuthor()
    {
        assertEquals("#1 The Hobbit by Tolkien", hobbit.toString());
    }


    @Test
    public void specialCharactersStoredAsIs()
    {
        Book b = new Book("Quotes \"and\", commas", "O'Brien", 3);
        assertEquals("Quotes \"and\", commas", b.getTitle());
        assertTrue(b.matches("o'brien"));
    }


    @Test
    public void comparatorsOrderByFieldThenCopyId()
    {
        Book a = new Book("alpha", "Zed", 5);
        Book z = new Book("Zeta", "adams", 4);
        assertTrue(Book.BY_TITLE.compare(a, z) < 0);
        assertTrue(Book.BY_AUTHOR.compare(z, a) < 0);
        assertTrue(Book.BY_NUMBER.compare(z, a) < 0);
        Book twin = new Book("alpha", "Zed", 6);
        assertTrue(Book.BY_TITLE.compare(a, twin) < 0);
    }
}
