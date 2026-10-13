package LinkedLibrary;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Library. Each public method has a normal case and a bad-input case;
 * the bad-input cases come from our scope document.
 */
public class LibraryTest
{
    private static final String HOBBIT = "The Hobbit";
    private static final String DUNE = "Dune";
    private Library lib;

    /** Two patrons and two copies of The Hobbit (#1, #2) plus Dune (#3). */
    @Before
    public void setUp()
    {
        lib = new Library();
        lib.addPatron("Ana");
        lib.addPatron("Bo");
        lib.addBook(HOBBIT, "Tolkien");
        lib.addBook(HOBBIT, "Tolkien");
        lib.addBook(DUNE, "Herbert");
    }

    // ----- addBook -----


    @Test
    public void addBookShelvesNewCopy()
    {
        assertEquals(Outcome.SUCCESS, lib.addBook("Anathem", "Stephenson"));
        assertEquals(4, lib.catalogSize());
        assertTrue(lib.isOnShelf(4));
    }


    @Test
    public void addBookRejectsBlankAndNull()
    {
        assertEquals(Outcome.INVALID_INPUT, lib.addBook("  ", "A"));
        assertEquals(Outcome.INVALID_INPUT, lib.addBook("T", null));
        assertEquals(3, lib.catalogSize());
    }


    @Test
    public void addBookGivesCopyToWaitingPatron()
    {
        lib.checkOut("Ana", DUNE);
        lib.placeHold("Bo", DUNE);
        assertEquals(Outcome.ASSIGNED_TO_HOLD, lib.addBook(DUNE, "Herbert"));
        assertTrue(lib.hasBorrowed("Bo", 4));
        assertEquals(0, lib.holdCount(DUNE));
    }

    // ----- removeBook -----


    @Test
    public void removeOneOfManyIdenticalCopies()
    {
        assertEquals(Outcome.SUCCESS, lib.removeBook(1));
        assertEquals(2, lib.catalogSize());
        assertTrue(lib.isOnShelf(2));   // the other Hobbit copy stays
    }


    @Test
    public void removeBookNotThere()
    {
        assertEquals(Outcome.BOOK_NOT_FOUND, lib.removeBook(99));
        assertEquals(Outcome.BOOK_NOT_FOUND, lib.removeBook(-1));
        assertEquals(3, lib.catalogSize());
    }


    @Test
    public void removeCheckedOutBookRefused()
    {
        lib.checkOut("Ana", DUNE);
        assertEquals(Outcome.HAS_DEPENDENTS, lib.removeBook(3));
        assertEquals(3, lib.catalogSize());
    }

    // ----- addPatron / removePatron -----


    @Test
    public void addPatronByName()
    {
        assertEquals(Outcome.SUCCESS, lib.addPatron("Cy"));
        assertEquals(3, lib.patronCount());
    }


    @Test
    public void addPatronRejectsDuplicateNameAndBlank()
    {
        assertEquals(Outcome.DUPLICATE_NAME, lib.addPatron(" ana "));
        assertEquals(Outcome.INVALID_INPUT, lib.addPatron(""));
        assertEquals(Outcome.INVALID_INPUT, lib.addPatron(null));
        assertEquals(2, lib.patronCount());
    }


    @Test
    public void removePatronWithNothingOut()
    {
        assertEquals(Outcome.SUCCESS, lib.removePatron("Bo"));
        assertEquals(1, lib.patronCount());
    }


    @Test
    public void removePatronNotThereOrWithDependents()
    {
        assertEquals(Outcome.PATRON_NOT_FOUND, lib.removePatron("Nobody"));
        assertEquals(Outcome.PATRON_NOT_FOUND, lib.removePatron(null));
        lib.checkOut("Ana", DUNE);
        assertEquals(Outcome.HAS_DEPENDENTS, lib.removePatron("Ana"));
        lib.placeHold("Bo", DUNE);
        assertEquals(Outcome.HAS_DEPENDENTS, lib.removePatron("Bo"));
        assertEquals(2, lib.patronCount());
    }


    @Test
    public void removeFromEmptyLibrary()
    {
        Library empty = new Library();
        assertEquals(Outcome.PATRON_NOT_FOUND, empty.removePatron("Ana"));
        assertEquals(Outcome.BOOK_NOT_FOUND, empty.removeBook(1));
    }

    // ----- checkOut -----


    @Test
    public void checkOutMovesCopyToPatron()
    {
        assertEquals(Outcome.SUCCESS, lib.checkOut("ANA", "dune"));
        assertFalse(lib.isOnShelf(3));
        assertTrue(lib.hasBorrowed("Ana", 3));
    }


    @Test
    public void checkOutBadInputChangesNothing()
    {
        assertEquals(Outcome.PATRON_NOT_FOUND, lib.checkOut("Nobody", DUNE));
        assertEquals(Outcome.BOOK_NOT_FOUND, lib.checkOut("Ana", "Nope"));
        assertEquals(Outcome.BOOK_NOT_FOUND, lib.checkOut("Ana", null));
        lib.checkOut("Ana", DUNE);
        assertEquals(Outcome.NONE_AVAILABLE, lib.checkOut("Bo", DUNE));
        assertEquals(0, lib.borrowedCount("Bo"));
    }

    // ----- returnBook -----


    @Test
    public void returnBookGoesBackToShelf()
    {
        lib.checkOut("Ana", DUNE);
        assertEquals(Outcome.SUCCESS, lib.returnBook("Ana", 3));
        assertTrue(lib.isOnShelf(3));
    }


    @Test
    public void returnBookAutoAssignsToEarliestHold()
    {
        lib.addPatron("Cy");
        lib.checkOut("Ana", DUNE);
        lib.placeHold("Bo", DUNE);
        lib.placeHold("Cy", DUNE);
        assertEquals(Outcome.ASSIGNED_TO_HOLD, lib.returnBook("Ana", 3));
        assertTrue(lib.hasBorrowed("Bo", 3));   // first in line
        assertEquals(1, lib.holdCount(DUNE));
    }


    @Test
    public void returnBookNeverBorrowedMovesNothing()
    {
        assertEquals(Outcome.NOT_BORROWED, lib.returnBook("Ana", 3));
        assertEquals(Outcome.PATRON_NOT_FOUND, lib.returnBook("Nobody", 3));
        assertEquals(Outcome.BOOK_NOT_FOUND, lib.returnBook("Ana", 99));
        assertTrue(lib.isOnShelf(3));
    }

    // ----- placeHold -----


    @Test
    public void placeHoldWhenAllCopiesOut()
    {
        lib.checkOut("Ana", DUNE);
        assertEquals(Outcome.SUCCESS, lib.placeHold("Bo", DUNE));
        assertEquals(1, lib.holdCount(DUNE));
    }


    @Test
    public void placeHoldBadInput()
    {
        assertEquals(Outcome.BOOK_AVAILABLE, lib.placeHold("Bo", DUNE));
        assertEquals(Outcome.PATRON_NOT_FOUND, lib.placeHold("Nobody", DUNE));
        assertEquals(Outcome.BOOK_NOT_FOUND, lib.placeHold("Bo", " "));
        lib.checkOut("Ana", DUNE);
        lib.placeHold("Bo", DUNE);
        assertEquals(Outcome.ALREADY_ON_HOLD, lib.placeHold("Bo", "DUNE"));
        assertEquals(1, lib.holdCount(DUNE));
    }

    // ----- undo -----


    @Test
    public void undoCheckout()
    {
        lib.checkOut("Ana", DUNE);
        assertEquals(Outcome.SUCCESS, lib.undo());
        assertTrue(lib.isOnShelf(3));
        assertEquals(0, lib.borrowedCount("Ana"));
    }


    @Test
    public void undoReturnGivesBookBack()
    {
        lib.checkOut("Ana", DUNE);
        lib.returnBook("Ana", 3);
        assertEquals(Outcome.SUCCESS, lib.undo());
        assertTrue(lib.hasBorrowed("Ana", 3));
        assertFalse(lib.isOnShelf(3));
    }


    @Test
    public void undoAutoAssignRestoresHoldAtFront()
    {
        lib.addPatron("Cy");
        lib.checkOut("Ana", DUNE);
        lib.placeHold("Bo", DUNE);
        lib.placeHold("Cy", DUNE);
        lib.returnBook("Ana", 3);              // goes to Bo
        assertEquals(Outcome.SUCCESS, lib.undo());
        assertTrue(lib.hasBorrowed("Ana", 3));
        assertEquals(2, lib.holdCount(DUNE));
        lib.returnBook("Ana", 3);              // Bo still first in line
        assertTrue(lib.hasBorrowed("Bo", 3));
    }


    @Test
    public void undoAddAndRemoveBook()
    {
        lib.removeBook(3);
        assertEquals(Outcome.SUCCESS, lib.undo());
        assertTrue(lib.isOnShelf(3));
        lib.addBook("Anathem", "Stephenson");
        assertEquals(Outcome.SUCCESS, lib.undo());
        assertEquals(3, lib.catalogSize());
    }


    @Test
    public void undoWithEmptyHistory()
    {
        Library fresh = new Library();
        assertEquals(Outcome.NOTHING_TO_UNDO, fresh.undo());
    }


    @Test
    public void undoNoLongerValidChangesNothing()
    {
        lib.checkOut("Bo", DUNE);
        lib.returnBook("Bo", 3);
        lib.removePatron("Bo");               // not undoable
        assertEquals(Outcome.UNDO_INVALID, lib.undo());
        assertTrue(lib.isOnShelf(3));
        assertEquals(1, lib.patronCount());
    }


    @Test
    public void historyCappedAtLimit()
    {
        for (int i = 0; i < 15; i++)
        {
            lib.checkOut("Ana", DUNE);
            lib.returnBook("Ana", 3);
        }
        assertEquals(Library.HISTORY_LIMIT, lib.historySize());
    }

    // ----- search -----


    @Test
    public void searchFindsAllMatchingCopies()
    {
        LinkedChain<Book> found = lib.search("hob");
        assertEquals(2, found.size());
    }


    @Test
    public void searchBlankOrMissingIsEmpty()
    {
        assertTrue(lib.search("zzz").isEmpty());
        assertTrue(lib.search("   ").isEmpty());
        assertTrue(lib.search(null).isEmpty());
        assertTrue(new Library().search("hob").isEmpty());
    }

    // ----- sortedShelf -----


    @Test
    public void sortedShelfByTitleLeavesShelfAlone()
    {
        Book[] byTitle = lib.sortedShelf(Book.BY_TITLE);
        assertEquals(DUNE, byTitle[0].getTitle());
        assertEquals(3, byTitle.length);
        Book[] arrival = lib.sortedShelf(Book.BY_NUMBER);
        assertEquals(1, arrival[0].getCopyNumber());
        assertEquals(3, lib.shelfSize());
    }


    @Test
    public void sortedShelfEmptyAndNullComparator()
    {
        assertEquals(0, new Library().sortedShelf(Book.BY_TITLE).length);
        try
        {
            lib.sortedShelf(null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(3, lib.shelfSize());
        }
    }

    // ----- due dates (stretch goal) -----


    @Test
    public void checkOutSetsDueDayAndReturnClearsIt()
    {
        lib.checkOut("Ana", DUNE);
        assertEquals(
            1 + Library.LOAN_DAYS,
            lib.search(DUNE).get(0).getDueDay());
        lib.returnBook("Ana", 3);
        assertEquals(0, lib.search(DUNE).get(0).getDueDay());
    }


    @Test
    public void autoAssignedCopyGetsFreshDueDay()
    {
        lib.checkOut("Ana", DUNE);
        lib.placeHold("Bo", DUNE);
        lib.advanceDays(5);
        lib.returnBook("Ana", 3);                 // goes to Bo on day 6
        assertEquals(
            6 + Library.LOAN_DAYS,
            lib.search(DUNE).get(0).getDueDay());
    }


    @Test
    public void advanceDaysMovesClock()
    {
        assertEquals(1, lib.getToday());
        assertEquals(Outcome.SUCCESS, lib.advanceDays(5));
        assertEquals(6, lib.getToday());
    }


    @Test
    public void advanceDaysRejectsZeroAndNegative()
    {
        assertEquals(Outcome.INVALID_INPUT, lib.advanceDays(0));
        assertEquals(Outcome.INVALID_INPUT, lib.advanceDays(-3));
        assertEquals(1, lib.getToday());
    }


    @Test
    public void overdueBooksMostLateFirst()
    {
        lib.checkOut("Ana", HOBBIT);              // due day 15
        lib.advanceDays(5);
        lib.checkOut("Bo", DUNE);                // due day 20
        lib.advanceDays(20);                     // today = 26
        Book[] late = lib.overdueBooks();
        assertEquals(2, late.length);
        assertEquals(HOBBIT, late[0].getTitle());
        assertEquals(11, late[0].daysLate(lib.getToday()));
        assertEquals(6, late[1].daysLate(lib.getToday()));
    }


    @Test
    public void overdueBooksEmptyWhenNothingLate()
    {
        assertEquals(0, lib.overdueBooks().length);     // nothing out
        lib.checkOut("Ana", DUNE);
        lib.advanceDays(Library.LOAN_DAYS);             // due today
        assertEquals(0, lib.overdueBooks().length);
        assertEquals(0, new Library().overdueBooks().length);
    }


    @Test
    public void returnedBookIsNoLongerOverdue()
    {
        lib.checkOut("Ana", DUNE);
        lib.advanceDays(30);
        assertEquals(1, lib.overdueBooks().length);
        lib.returnBook("Ana", 3);
        assertEquals(0, lib.overdueBooks().length);
    }


    @Test
    public void undoReturnRestoresOriginalDueDay()
    {
        lib.checkOut("Ana", DUNE);                // due day 15
        lib.advanceDays(30);
        lib.returnBook("Ana", 3);
        lib.undo();
        assertEquals(1, lib.overdueBooks().length);
        assertEquals(15, lib.overdueBooks()[0].getDueDay());
    }


    @Test
    public void borrowerOfShowsWhoHasIt()
    {
        lib.checkOut("Ana", DUNE);
        assertEquals("Ana", lib.borrowerOf(3));
    }


    @Test
    public void borrowerOfNullWhenShelvedOrMissing()
    {
        assertNull(lib.borrowerOf(3));
        assertNull(lib.borrowerOf(99));
    }

    // ----- queries -----


    @Test
    public void queriesOnKnownItems()
    {
        assertEquals(3, lib.catalogSize());
        assertEquals(3, lib.shelfSize());
        assertEquals(2, lib.patronCount());
        assertEquals(3, lib.historySize());     // three ADD_BOOKs
        assertEquals(0, lib.borrowedCount("Ana"));
        assertTrue(lib.isOnShelf(1));
    }


    @Test
    public void queriesOnUnknownItems()
    {
        assertEquals(-1, lib.borrowedCount("Nobody"));
        assertEquals(-1, lib.borrowedCount(null));
        assertFalse(lib.isOnShelf(42));
        assertFalse(lib.hasBorrowed("Nobody", 1));
        assertFalse(lib.hasBorrowed("Ana", 42));
        assertEquals(0, lib.holdCount(null));
        assertEquals(0, lib.holdCount("Nope"));
    }

    // ----- invariant stress test -----


    @Test
    public void rapidCheckoutReturnUndoKeepsEveryCopyInOnePlace()
    {
        String[] ids = { "Ana", "Bo" };
        for (int i = 0; i < 200; i++)
        {
            String id = ids[i % 2];
            switch (i % 5)
            {
                case 0:
                    lib.checkOut(id, HOBBIT);
                    break;
                case 1:
                    lib.returnBook(id, 1 + i % 3);
                    break;
                case 2:
                    lib.placeHold(id, HOBBIT);
                    break;
                case 3:
                    lib.undo();
                    break;
                default:
                    lib.checkOut(id, DUNE);
                    break;
            }
            int placed = lib.shelfSize() + lib.borrowedCount("Ana")
                + lib.borrowedCount("Bo");
            assertEquals("step " + i, lib.catalogSize(), placed);
        }
    }
}
