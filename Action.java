package LinkedLibrary;

/**
 * An immutable record of one undoable event. It stores everything Library needs
 * to reverse the event. Library does the reversing.
 */
public class Action
{
    private final ActionType type;
    private final Book book;
    private final Patron patron;
    private final int dueDay;
    private final HoldRequest filledHold;

    /**
     * @param type
     *            what happened
     * @param book
     *            the copy involved
     * @param patron
     *            who checked out or returned; null for add/remove
     * @param dueDay
     *            the copy's due day before a return (so undo can restore it); 0
     *            otherwise
     * @param filledHold
     *            the hold that received the copy during this action, or null if
     *            the copy was not auto-assigned
     * @throws IllegalArgumentException
     *             if type or book is null
     */
    public Action(
        ActionType type,
        Book book,
        Patron patron,
        int dueDay,
        HoldRequest filledHold)
    {
        if (type == null || book == null)
        {
            throw new IllegalArgumentException("Type and book are required");
        }
        this.type = type;
        this.book = book;
        this.patron = patron;
        this.dueDay = dueDay;
        this.filledHold = filledHold;
    }


    public ActionType getType()
    {
        return type;
    }


    public Book getBook()
    {
        return book;
    }


    public Patron getPatron()
    {
        return patron;
    }


    public int getDueDay()
    {
        return dueDay;
    }


    public HoldRequest getFilledHold()
    {
        return filledHold;
    }


    @Override
    public String toString()
    {
        return type + " " + book + (patron == null ? "" : " / " + patron);
    }
}
