package LinkedLibrary;

/**
 * One patron waiting for any copy of one title. The sequence number is the
 * patron's place in line (lower = earlier), so the waiting list stays fair even
 * after an undo puts a hold back.
 */
public class HoldRequest
{
    private final Patron patron;
    private final String title;
    private final int sequence;

    /**
     * @throws IllegalArgumentException
     *             if patron is null, the title is blank, or the sequence is not
     *             positive
     */
    public HoldRequest(Patron patron, String title, int sequence)
    {
        if (patron == null || Book.isBlank(title))
        {
            throw new IllegalArgumentException("Patron and title are required");
        }
        if (sequence < 1)
        {
            throw new IllegalArgumentException("Sequence must be positive");
        }
        this.patron = patron;
        this.title = title.trim();
        this.sequence = sequence;
    }


    public Patron getPatron()
    {
        return patron;
    }


    public String getTitle()
    {
        return title;
    }


    public int getSequence()
    {
        return sequence;
    }


    /** True if this hold is for the given title (ignoring case). */
    public boolean isFor(String otherTitle)
    {
        return !Book.isBlank(otherTitle)
            && title.equalsIgnoreCase(otherTitle.trim());
    }


    /** Same patron waiting for the same title = duplicate hold. */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof HoldRequest))
        {
            return false;
        }
        HoldRequest h = (HoldRequest)other;
        return patron.equals(h.patron) && title.equalsIgnoreCase(h.title);
    }


    @Override
    public int hashCode()
    {
        return 31 * patron.hashCode() + title.toLowerCase().hashCode();
    }


    @Override
    public String toString()
    {
        return patron + " waiting for " + title;
    }
}
