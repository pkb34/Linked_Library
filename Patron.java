package LinkedLibrary;

/**
 * One library member and the copies they currently have checked out. A patron
 * is identified by name (ignoring case), so names must be unique.
 */
public class Patron
{
    private final String name;
    private final LinkedChain<Book> borrowed;

    /**
     * @throws IllegalArgumentException
     *             if the name is null/blank
     */
    public Patron(String name)
    {
        if (Book.isBlank(name))
        {
            throw new IllegalArgumentException("Name is required");
        }
        this.name = name.trim();
        this.borrowed = new LinkedChain<Book>();
    }


    public String getName()
    {
        return name;
    }


    /** True if this patron has the given name (ignoring case and spaces). */
    public boolean hasName(String otherName)
    {
        return !Book.isBlank(otherName)
            && name.equalsIgnoreCase(otherName.trim());
    }


    /**
     * Adds a copy to this patron's borrowed list.
     *
     * @throws IllegalArgumentException
     *             if book is null or the patron already has this copy
     */
    public void borrow(Book book)
    {
        if (book == null)
        {
            throw new IllegalArgumentException("Book cannot be null");
        }
        if (borrowed.contains(book))
        {
            throw new IllegalArgumentException("Already borrowed");
        }
        borrowed.add(book);
    }


    /** Removes the copy; returns false if this patron did not have it. */
    public boolean giveBack(Book book)
    {
        return borrowed.remove(book);   // remove(null) returns false
    }


    /** True if this patron currently has that copy. */
    public boolean hasBorrowed(Book book)
    {
        return borrowed.contains(book); // contains(null) returns false
    }


    /** Number of copies checked out. */
    public int borrowedCount()
    {
        return borrowed.size();
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof Patron))
        {
            return false;
        }
        return name.equalsIgnoreCase(((Patron)other).name);
    }


    @Override
    public int hashCode()
    {
        return name.toLowerCase().hashCode();
    }


    @Override
    public String toString()
    {
        return name;
    }
}
