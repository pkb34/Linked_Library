package LinkedLibrary;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/** Tests for Patron. */
public class PatronTest
{
    private Patron ana;
    private Book hobbit;
    private Book dune;

    @Before
    public void setUp()
    {
        ana = new Patron(" Ana ");
        hobbit = new Book("The Hobbit", "Tolkien", 1);
        dune = new Book("Dune", "Herbert", 2);
    }


    @Test
    public void constructorTrims()
    {
        assertEquals("Ana", ana.getName());
        assertEquals(0, ana.borrowedCount());
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsBlankName()
    {
        new Patron("   ");
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullName()
    {
        new Patron(null);
    }


    @Test
    public void hasNameIgnoresCaseAndSpaces()
    {
        assertTrue(ana.hasName("ana"));
        assertTrue(ana.hasName("  ANA "));
    }


    @Test
    public void hasNameRejectsOtherNullAndBlank()
    {
        assertFalse(ana.hasName("Bo"));
        assertFalse(ana.hasName(null));
        assertFalse(ana.hasName(""));
    }


    @Test
    public void borrowAddsCopy()
    {
        ana.borrow(hobbit);
        assertTrue(ana.hasBorrowed(hobbit));
        assertEquals(1, ana.borrowedCount());
    }


    @Test(expected = IllegalArgumentException.class)
    public void borrowRejectsNull()
    {
        ana.borrow(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void borrowRejectsSameCopyTwice()
    {
        ana.borrow(hobbit);
        ana.borrow(hobbit);
    }


    @Test
    public void giveBackRemovesOnlyThatCopy()
    {
        ana.borrow(hobbit);
        ana.borrow(dune);
        assertTrue(ana.giveBack(hobbit));
        assertFalse(ana.hasBorrowed(hobbit));
        assertTrue(ana.hasBorrowed(dune));
    }


    @Test
    public void giveBackSomethingNotBorrowedReturnsFalse()
    {
        assertFalse(ana.giveBack(hobbit));   // empty collection
        ana.borrow(dune);
        assertFalse(ana.giveBack(hobbit));   // not there
        assertFalse(ana.giveBack(null));
        assertEquals(1, ana.borrowedCount());
    }


    @Test
    public void hasBorrowedFalseForNullAndMissing()
    {
        assertFalse(ana.hasBorrowed(null));
        assertFalse(ana.hasBorrowed(dune));
    }


    @Test
    public void equalsUsesNameIgnoringCase()
    {
        assertEquals(ana, new Patron("ANA"));
        assertEquals(ana.hashCode(), new Patron("ana").hashCode());
        assertNotEquals(ana, new Patron("Bo"));
    }


    @Test
    public void equalsRejectsNullAndOtherTypes()
    {
        assertFalse(ana.equals(null));
        assertFalse(ana.equals("Ana"));
    }


    @Test
    public void toStringIsName()
    {
        assertEquals("Ana", ana.toString());
    }
}
