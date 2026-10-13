package LinkedLibrary;

import java.util.Comparator;

/**
 * One physical copy of a book: title, author, and its copy number (the number
 * on its shelf label). Two copies of the same title are different Books because
 * equality is based on the copy number. A copy that is checked out has a due
 * day; a copy on the shelf has due day 0.
 */
public class Book
{
    /** Arrival order (copy numbers are handed out in order). */
    public static final Comparator<Book> BY_NUMBER =
        Comparator.comparingInt(Book::getCopyNumber);
    /** A to Z by title, ignoring case; ties broken by copy number. */
    public static final Comparator<Book> BY_TITLE = Comparator
        .comparing((Book b) -> b.title.toLowerCase()).thenComparing(BY_NUMBER);
    /** A to Z by author, ignoring case; ties broken by copy number. */
    public static final Comparator<Book> BY_AUTHOR = Comparator
        .comparing((Book b) -> b.author.toLowerCase()).thenComparing(BY_NUMBER);

    private final String title;
    private final String author;
    private final int copyNumber;
    private int dueDay;   // 0 = not checked out

    /**
     * Creates a copy.
     *
     * @throws IllegalArgumentException
     *             if title or author is null/blank or the copy number is not
     *             positive
     */
    public Book(String title, String author, int copyNumber)
    {
        if (isBlank(title) || isBlank(author))
        {
            throw new IllegalArgumentException("Title and author are required");
        }
        if (copyNumber < 1)
        {
            throw new IllegalArgumentException("Copy number must be positive");
        }
        this.title = title.trim();
        this.author = author.trim();
        this.copyNumber = copyNumber;
        this.dueDay = 0;
    }


    /** True if s is null or only whitespace. Shared by every app class. */
    public static boolean isBlank(String s)
    {
        return s == null || s.trim().isEmpty();
    }


    public String getTitle()
    {
        return title;
    }


    public String getAuthor()
    {
        return author;
    }


    public int getCopyNumber()
    {
        return copyNumber;
    }


    /** The day this copy is due back, or 0 if it is not checked out. */
    public int getDueDay()
    {
        return dueDay;
    }


    /**
     * Sets the due day; use 0 when the copy goes back on the shelf.
     *
     * @throws IllegalArgumentException
     *             if day is negative
     */
    public void setDueDay(int day)
    {
        if (day < 0)
        {
            throw new IllegalArgumentException("Due day cannot be negative");
        }
        dueDay = day;
    }


    /** Days past due on the given day; 0 if not late or not checked out. */
    public int daysLate(int today)
    {
        if (dueDay == 0)
        {
            return 0;
        }
        return Math.max(0, today - dueDay);
    }


    /** True if this copy is the given title (ignoring case and spaces). */
    public boolean hasTitle(String otherTitle)
    {
        return !isBlank(otherTitle)
            && title.equalsIgnoreCase(otherTitle.trim());
    }


    /**
     * True if the title or author contains the query (case-insensitive). A null
     * or blank query matches nothing.
     */
    public boolean matches(String query)
    {
        if (isBlank(query))
        {
            return false;
        }
        String q = query.trim().toLowerCase();
        return title.toLowerCase().contains(q)
            || author.toLowerCase().contains(q);
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof Book))
        {
            return false;
        }
        return copyNumber == ((Book)other).copyNumber;
    }


    @Override
    public int hashCode()
    {
        return Integer.hashCode(copyNumber);
    }


    @Override
    public String toString()
    {
        return "#" + copyNumber + " " + title + " by " + author;
    }
}
