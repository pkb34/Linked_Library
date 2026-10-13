package LinkedLibrary;

import java.util.Comparator;

/**
 * All library rules: books, patrons, checkouts, returns, holds, undo, search,
 * sorting, and due dates. Library owns every LinkedChain and uses them only
 * through their public methods. It never reads input or prints; it returns an
 * Outcome (or data) and LibraryMenu talks to the user. Invariant: every copy in
 * the catalog is in exactly one place, either on the shelf or in one patron's
 * borrowed list.
 */
public class Library
{
    /** How many actions undo remembers. */
    public static final int HISTORY_LIMIT = 10;
    /** How many days a checkout lasts. */
    public static final int LOAN_DAYS = 14;

    private final LinkedChain<Book> catalog;       // every copy owned
    private final LinkedChain<Book> shelf;         // copies available now
    private final LinkedChain<Patron> patrons;
    private final LinkedChain<HoldRequest> holds;  // arrival order
    private final LinkedChain<Action> history;     // front = newest
    private int nextCopyId;
    private int nextHoldNumber;
    private int today;   // the library's day counter, starts at day 1

    public Library()
    {
        catalog = new LinkedChain<Book>();
        shelf = new LinkedChain<Book>();
        patrons = new LinkedChain<Patron>();
        holds = new LinkedChain<HoldRequest>();
        history = new LinkedChain<Action>();
        nextCopyId = 1;
        nextHoldNumber = 1;
        today = 1;
    }

    // ----- books -----


    /**
     * Adds a new copy. If someone is waiting for that title, the copy goes
     * straight to the earliest hold; otherwise it goes on the shelf.
     */
    public Outcome addBook(String title, String author)
    {
        if (Book.isBlank(title) || Book.isBlank(author))
        {
            return Outcome.INVALID_INPUT;
        }
        Book book = new Book(title, author, nextCopyId++);
        catalog.add(book);
        HoldRequest filled = placeCopy(book);
        record(ActionType.ADD_BOOK, book, null, 0, filled);
        return filled == null ? Outcome.SUCCESS : Outcome.ASSIGNED_TO_HOLD;
    }


    /** Removes a copy that is on the shelf. Checked-out copies are refused. */
    public Outcome removeBook(int copyNumber)
    {
        Book book = findCopy(copyNumber);
        if (book == null)
        {
            return Outcome.BOOK_NOT_FOUND;
        }
        if (!shelf.contains(book))
        {
            return Outcome.HAS_DEPENDENTS;
        }
        shelf.remove(book);
        catalog.remove(book);
        record(ActionType.REMOVE_BOOK, book, null, 0, null);
        return Outcome.SUCCESS;
    }

    // ----- patrons -----


    /** Adds a member. Names must be unique (ignoring case). */
    public Outcome addPatron(String name)
    {
        if (Book.isBlank(name))
        {
            return Outcome.INVALID_INPUT;
        }
        if (findPatron(name) != null)
        {
            return Outcome.DUPLICATE_NAME;
        }
        patrons.add(new Patron(name));
        return Outcome.SUCCESS;
    }


    /** Removes a member with no checkouts and no holds. */
    public Outcome removePatron(String name)
    {
        Patron patron = findPatron(name);
        if (patron == null)
        {
            return Outcome.PATRON_NOT_FOUND;
        }
        boolean hasHold = ChainTools
            .findFirst(holds, h -> h.getPatron().equals(patron)) != null;
        if (patron.borrowedCount() > 0 || hasHold)
        {
            return Outcome.HAS_DEPENDENTS;
        }
        patrons.remove(patron);
        return Outcome.SUCCESS;
    }

    // ----- checkout, return, holds -----


    /** Moves any shelved copy of the title to the patron. */
    public Outcome checkOut(String name, String title)
    {
        Patron patron = findPatron(name);
        if (patron == null)
        {
            return Outcome.PATRON_NOT_FOUND;
        }
        if (!titleExists(title))
        {
            return Outcome.BOOK_NOT_FOUND;
        }
        Book copy = ChainTools.findFirst(shelf, b -> b.hasTitle(title));
        if (copy == null)
        {
            return Outcome.NONE_AVAILABLE;
        }
        shelf.remove(copy);
        patron.borrow(copy);
        copy.setDueDay(today + LOAN_DAYS);
        record(ActionType.CHECKOUT, copy, patron, 0, null);
        return Outcome.SUCCESS;
    }


    /**
     * Takes a copy back from the patron. If anyone is waiting for that title,
     * the copy goes to the earliest hold; otherwise to the shelf.
     */
    public Outcome returnBook(String name, int copyNumber)
    {
        Patron patron = findPatron(name);
        if (patron == null)
        {
            return Outcome.PATRON_NOT_FOUND;
        }
        Book book = findCopy(copyNumber);
        if (book == null)
        {
            return Outcome.BOOK_NOT_FOUND;
        }
        if (!patron.giveBack(book))
        {
            return Outcome.NOT_BORROWED;
        }
        int oldDueDay = book.getDueDay();
        HoldRequest filled = placeCopy(book);
        record(ActionType.RETURN, book, patron, oldDueDay, filled);
        return filled == null ? Outcome.SUCCESS : Outcome.ASSIGNED_TO_HOLD;
    }


    /** Adds the patron to a title's waiting list when no copy is shelved. */
    public Outcome placeHold(String name, String title)
    {
        Patron patron = findPatron(name);
        if (patron == null)
        {
            return Outcome.PATRON_NOT_FOUND;
        }
        if (!titleExists(title))
        {
            return Outcome.BOOK_NOT_FOUND;
        }
        if (ChainTools.findFirst(shelf, b -> b.hasTitle(title)) != null)
        {
            return Outcome.BOOK_AVAILABLE;
        }
        HoldRequest request = new HoldRequest(patron, title, nextHoldNumber);
        if (holds.contains(request))
        {
            return Outcome.ALREADY_ON_HOLD;
        }
        nextHoldNumber++;
        holds.addToEnd(request);
        return Outcome.SUCCESS;
    }

    // ----- undo -----


    /**
     * Reverses the most recent checkout, return, add, or remove. If the action
     * can no longer be reversed (for example, the patron was removed since),
     * nothing changes and the action is dropped from history.
     */
    public Outcome undo()
    {
        Action last = history.remove();
        if (last == null)
        {
            return Outcome.NOTHING_TO_UNDO;
        }
        return reverse(last) ? Outcome.SUCCESS : Outcome.UNDO_INVALID;
    }


    private boolean reverse(Action a)
    {
        Book book = a.getBook();
        Patron patron = a.getPatron();
        HoldRequest hold = a.getFilledHold();
        switch (a.getType())
        {
            case CHECKOUT:
                if (!patrons.contains(patron) || !patron.hasBorrowed(book))
                {
                    return false;
                }
                patron.giveBack(book);
                placeCopy(book);
                return true;
            case RETURN:
                if (!patrons.contains(patron) || !canRecall(book, hold))
                {
                    return false;
                }
                recall(book, hold);
                patron.borrow(book);
                book.setDueDay(a.getDueDay());
                return true;
            case ADD_BOOK:
                if (!canRecall(book, hold))
                {
                    return false;
                }
                recall(book, hold);
                catalog.remove(book);
                return true;
            case REMOVE_BOOK:
                if (catalog.contains(book))
                {
                    return false;
                }
                catalog.add(book);
                placeCopy(book);
                return true;
            default:
                return false;
        }
    }


    /** True if the copy is where the action left it (shelf or hold patron). */
    private boolean canRecall(Book book, HoldRequest hold)
    {
        if (hold == null)
        {
            return shelf.contains(book);
        }
        return patrons.contains(hold.getPatron())
            && hold.getPatron().hasBorrowed(book);
    }


    /** Takes the copy back from where the action put it. */
    private void recall(Book book, HoldRequest hold)
    {
        if (hold == null)
        {
            shelf.remove(book);
        }
        else
        {
            hold.getPatron().giveBack(book);
            holds.addToEnd(hold);   // same sequence = same place in line
        }
    }


    private void record(
        ActionType type,
        Book book,
        Patron patron,
        int dueDay,
        HoldRequest filled)
    {
        history.add(new Action(type, book, patron, dueDay, filled));
        if (history.size() > HISTORY_LIMIT)
        {
            history.remove(history.get(history.size() - 1));
        }
    }

    // ----- due dates (stretch goal) -----


    /** Moves the library's day counter forward. */
    public Outcome advanceDays(int days)
    {
        if (days < 1)
        {
            return Outcome.INVALID_INPUT;
        }
        today += days;
        return Outcome.SUCCESS;
    }


    public int getToday()
    {
        return today;
    }


    /** Every late copy, most days late first (ties by copy number). */
    public Book[] overdueBooks()
    {
        LinkedChain<Book> late =
            ChainTools.findAll(catalog, b -> b.daysLate(today) > 0);
        Object[] sorted = ChainTools.sortedSnapshot(
            late,
            Comparator.comparingInt((Book b) -> -b.daysLate(today))
                .thenComparing(Book.BY_NUMBER));
        Book[] result = new Book[sorted.length];
        for (int i = 0; i < sorted.length; i++)
        {
            result[i] = (Book)sorted[i];
        }
        return result;
    }


    /** The name of whoever has the copy, or null if nobody does. */
    public String borrowerOf(int copyNumber)
    {
        Book book = findCopy(copyNumber);
        if (book == null)
        {
            return null;
        }
        Patron who = ChainTools.findFirst(patrons, p -> p.hasBorrowed(book));
        return who == null ? null : who.getName();
    }

    // ----- queries (no state changes) -----


    /** Every copy whose title or author contains the query. */
    public LinkedChain<Book> search(String query)
    {
        return ChainTools.findAll(catalog, b -> b.matches(query));
    }


    /**
     * A sorted copy of the shelf. The shelf itself is not reordered.
     * Book.BY_NUMBER gives arrival order.
     *
     * @throws IllegalArgumentException
     *             if order is null
     */
    public Book[] sortedShelf(Comparator<Book> order)
    {
        Object[] sorted = ChainTools.sortedSnapshot(shelf, order);
        Book[] result = new Book[sorted.length];
        for (int i = 0; i < sorted.length; i++)
        {
            result[i] = (Book)sorted[i];
        }
        return result;
    }


    /** Number of patrons waiting for a title. */
    public int holdCount(String title)
    {
        return ChainTools.findAll(holds, h -> h.isFor(title)).size();
    }


    /** True if the copy is on the shelf. */
    public boolean isOnShelf(int copyNumber)
    {
        Book book = findCopy(copyNumber);
        return book != null && shelf.contains(book);
    }


    /** True if the patron has that copy checked out. */
    public boolean hasBorrowed(String name, int copyNumber)
    {
        Patron patron = findPatron(name);
        Book book = findCopy(copyNumber);
        return patron != null && book != null && patron.hasBorrowed(book);
    }


    /** Copies a patron has out, or -1 if the patron does not exist. */
    public int borrowedCount(String name)
    {
        Patron patron = findPatron(name);
        return patron == null ? -1 : patron.borrowedCount();
    }


    public int catalogSize()
    {
        return catalog.size();
    }


    public int shelfSize()
    {
        return shelf.size();
    }


    public int patronCount()
    {
        return patrons.size();
    }


    public int historySize()
    {
        return history.size();
    }

    // ----- private helpers -----


    /**
     * Gives a copy to the earliest hold for its title (with a fresh due day),
     * else shelves it (due day cleared).
     *
     * @return the hold that was filled, or null if the copy was shelved
     */
    private HoldRequest placeCopy(Book book)
    {
        HoldRequest next = earliestHold(book.getTitle());
        if (next == null)
        {
            book.setDueDay(0);
            shelf.add(book);
            return null;
        }
        holds.remove(next);
        next.getPatron().borrow(book);
        book.setDueDay(today + LOAN_DAYS);
        return next;
    }


    /** Lowest sequence number among holds for a title, or null. */
    private HoldRequest earliestHold(String title)
    {
        HoldRequest best = null;
        for (Object o : ChainTools.snapshot(holds))
        {
            HoldRequest h = (HoldRequest)o;
            if (h.isFor(title)
                && (best == null || h.getSequence() < best.getSequence()))
            {
                best = h;
            }
        }
        return best;
    }


    private Patron findPatron(String name)
    {
        return ChainTools.findFirst(patrons, p -> p.hasName(name));
    }


    private Book findCopy(int copyNumber)
    {
        return ChainTools
            .findFirst(catalog, b -> b.getCopyNumber() == copyNumber);
    }


    private boolean titleExists(String title)
    {
        return ChainTools.findFirst(catalog, b -> b.hasTitle(title)) != null;
    }
}
