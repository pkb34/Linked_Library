package LinkedLibrary;

import static org.junit.Assert.*;

import org.junit.Test;

/** Tests for Action, ActionType, and Outcome. */
public class ActionTest {
    private final Book book = new Book("Dune", "Herbert", 2);
    private final Patron bo = new Patron("Bo");

    @Test
    public void gettersReturnConstructorValues() {
        HoldRequest hold = new HoldRequest(bo, "Dune", 1);
        Action a = new Action(ActionType.RETURN, book, bo, 15, hold);
        assertEquals(ActionType.RETURN, a.getType());
        assertEquals(15, a.getDueDay());
        assertSame(book, a.getBook());
        assertSame(bo, a.getPatron());
        assertSame(hold, a.getFilledHold());
    }

    @Test
    public void addBookActionHasNoPatronOrHold() {
        Action a = new Action(ActionType.ADD_BOOK, book, null, 0, null);
        assertNull(a.getPatron());
        assertNull(a.getFilledHold());
        assertEquals("ADD_BOOK #2 Dune by Herbert", a.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullType() {
        new Action(null, book, bo, 0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullBook() {
        new Action(ActionType.CHECKOUT, null, bo, 0, null);
    }

    @Test
    public void toStringIncludesPatron() {
        Action a = new Action(ActionType.CHECKOUT, book, bo, 0, null);
        assertEquals("CHECKOUT #2 Dune by Herbert / Bo", a.toString());
    }

    @Test
    public void actionTypeHasFourKinds() {
        assertEquals(4, ActionType.values().length);
        assertEquals(ActionType.CHECKOUT, ActionType.valueOf("CHECKOUT"));
    }

    @Test
    public void everyOutcomeHasAMessage() {
        for (Outcome o : Outcome.values()) {
            assertFalse(Book.isBlank(o.getMessage()));
        }
        assertEquals("Nothing to undo.", Outcome.NOTHING_TO_UNDO.getMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void outcomeValueOfRejectsUnknownName() {
        Outcome.valueOf("NOT_A_REAL_OUTCOME");
    }
}
