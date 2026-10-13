package LinkedLibrary;

/**
 * One checked-out copy: which copy, who has it, and the day it is due. A copy
 * can be on only one loan at a time, so equality is by book.
 */
public class Loan
{
    private final Book book;
    private final Patron patron;
    private final int dueDay;

    /**
     * @throws IllegalArgumentException
     *             if book or patron is null, or the due day is not positive
     */
    public Loan(Book book, Patron patron, int dueDay)
    {
        if (book == null || patron == null)
        {
            throw new IllegalArgumentException("Book and patron are required");
        }
        if (dueDay < 1)
        {
            throw new IllegalArgumentException("Due day must be positive");
        }
        this.book = book;
        this.patron = patron;
        this.dueDay = dueDay;
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


    /** Days past due on the given day; 0 if not late. */
    public int daysLate(int today)
    {
        return Math.max(0, today - dueDay);
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof Loan))
        {
            return false;
        }
        return book.equals(((Loan)other).book);
    }


    @Override
    public int hashCode()
    {
        return book.hashCode();
    }


    @Override
    public String toString()
    {
        return book + " - " + patron + ", due day " + dueDay;
    }
}
