import java.util.Comparator;

/**
 * All library rules: books, patrons, checkouts, returns, holds, undo, sorting,
 * and due dates. Library owns every LinkedChain and uses them only through
 * their public methods. It never reads input or prints; it returns an Outcome
 * (or data) and LibraryMenu talks to the user. Invariant: every copy in the
 * catalog is in exactly one place, either on the shelf or on one patron's loan
 * list.
 */
public class Library
{
    /** Days a checkout lasts. */
    public static final int LOAN_DAYS = 14;
    /** How many actions undo remembers. */
    public static final int HISTORY_LIMIT = 10;

    private final LinkedChain<Book> catalog;       // every copy owned
    private final LinkedChain<Book> shelf;         // copies available now
    private final LinkedChain<Patron> patrons;
    private final LinkedChain<HoldRequest> holds;  // arrival order
    private final LinkedChain<Action> history;     // front = newest
    private int nextCopyId;
    private int nextHoldNumber;
    private int nextActionNumber;
    private int today;

    public Library()
    {
        catalog = new LinkedChain<Book>();
        shelf = new LinkedChain<Book>();
        patrons = new LinkedChain<Patron>();
        holds = new LinkedChain<HoldRequest>();
        history = new LinkedChain<Action>();
        nextCopyId = 1;
        nextHoldNumber = 1;
        nextActionNumber = 1;
        today = 1;
    }

    // ----- books -----


    /**
     * Adds a new copy. If someone is waiting for that title, the copy goes
     * straight to the earliest hold; otherwise it goes on the shelf.
     */
    public Outcome addBook(String title, String author, String callNumber)
    {
        if (Book.isBlank(title) || Book.isBlank(author)
            || Book.isBlank(callNumber))
        {
            return Outcome.INVALID_INPUT;
        }
        Book book = new Book(title, author, callNumber, nextCopyId++);
        catalog.add(book);
        HoldRequest filled = placeCopy(book);
        record(ActionType.ADD_BOOK, book, null, 0, filled);
        return filled == null ? Outcome.SUCCESS : Outcome.ASSIGNED_TO_HOLD;
    }


    /** Removes a copy that is on the shelf. Checked-out copies are refused. */
    public Outcome removeBook(int copyId)
    {
        Book book = findCopy(copyId);
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


    /** Adds a member. IDs must be unique; names may repeat. */
    public Outcome addPatron(String patronId, String name)
    {
        if (Book.isBlank(patronId) || Book.isBlank(name))
        {
            return Outcome.INVALID_INPUT;
        }
        if (findPatron(patronId) != null)
        {
            return Outcome.DUPLICATE_ID;
        }
        patrons.add(new Patron(patronId, name));
        return Outcome.SUCCESS;
    }


    /** Removes a member with no loans and no holds. */
    public Outcome removePatron(String patronId)
    {
        Patron patron = findPatron(patronId);
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
    public Outcome checkOut(String patronId, String callNumber)
    {
        Patron patron = findPatron(patronId);
        if (patron == null)
        {
            return Outcome.PATRON_NOT_FOUND;
        }
        if (!titleExists(callNumber))
        {
            return Outcome.BOOK_NOT_FOUND;
        }
        String call = callNumber.trim();
        Book copy =
            ChainTools.findFirst(shelf, b -> b.getCallNumber().equals(call));
        if (copy == null)
        {
            return Outcome.NONE_AVAILABLE;
        }
        int due = today + LOAN_DAYS;
        shelf.remove(copy);
        patron.borrow(copy, due);
        record(ActionType.CHECKOUT, copy, patron, due, null);
        return Outcome.SUCCESS;
    }


    /**
     * Takes a copy back from the patron. If anyone is waiting for that title,
     * the copy goes to the earliest hold; otherwise to the shelf.
     */
    public Outcome returnBook(String patronId, int copyId)
    {
        Patron patron = findPatron(patronId);
        if (patron == null)
        {
            return Outcome.PATRON_NOT_FOUND;
        }
        Book book = findCopy(copyId);
        if (book == null)
        {
            return Outcome.BOOK_NOT_FOUND;
        }
        Loan loan = patron.giveBack(book);
        if (loan == null)
        {
            return Outcome.NOT_BORROWED;
        }
        HoldRequest filled = placeCopy(book);
        record(ActionType.RETURN, book, patron, loan.getDueDay(), filled);
        return filled == null ? Outcome.SUCCESS : Outcome.ASSIGNED_TO_HOLD;
    }


    /** Adds the patron to a title's waiting list when no copy is shelved. */
    public Outcome placeHold(String patronId, String callNumber)
    {
        Patron patron = findPatron(patronId);
        if (patron == null)
        {
            return Outcome.PATRON_NOT_FOUND;
        }
        if (!titleExists(callNumber))
        {
            return Outcome.BOOK_NOT_FOUND;
        }
        String call = callNumber.trim();
        if (ChainTools
            .findFirst(shelf, b -> b.getCallNumber().equals(call)) != null)
        {
            return Outcome.BOOK_AVAILABLE;
        }
        HoldRequest request = new HoldRequest(patron, call, nextHoldNumber);
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
        HoldRequest hold = a.getFilledHold();
        switch (a.getType())
        {
            case CHECKOUT:
                if (!patrons.contains(a.getPatron())
                    || !a.getPatron().hasBorrowed(book))
                {
                    return false;
                }
                a.getPatron().giveBack(book);
                placeCopy(book);
                return true;
            case RETURN:
                if (!patrons.contains(a.getPatron()) || !canRecall(book, hold))
                {
                    return false;
                }
                recall(book, hold);
                a.getPatron().borrow(book, a.getDueDay());
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
        int due,
        HoldRequest filled)
    {
        history.add(
            new Action(nextActionNumber++, type, book, patron, due, filled));
        if (history.size() > HISTORY_LIMIT)
        {
            history.remove(history.get(history.size() - 1));
        }
    }

    // ----- due dates (stretch goal) -----


    /** Moves the library's clock forward. */
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


    /** Every late loan, most days late first. */
    public Loan[] overdueLoans()
    {
        LinkedChain<Loan> late = new LinkedChain<Loan>();
        for (Object o : ChainTools.snapshot(patrons))
        {
            for (Object l : ChainTools
                .snapshot(((Patron)o).overdueLoans(today)))
            {
                late.add((Loan)l);
            }
        }
        Object[] sorted = ChainTools.sortedSnapshot(
            late,
            Comparator.comparingInt((Loan l) -> -l.daysLate(today))
                .thenComparingInt(l -> l.getBook().getCopyId()));
        Loan[] result = new Loan[sorted.length];
        for (int i = 0; i < sorted.length; i++)
        {
            result[i] = (Loan)sorted[i];
        }
        return result;
    }

    // ----- queries (no state changes) -----


    /** Every copy whose title or author contains the query. */
    public LinkedChain<Book> search(String query)
    {
        return ChainTools.findAll(catalog, b -> b.matches(query));
    }


    /**
     * A sorted copy of the shelf. The shelf itself is not reordered.
     * Book.BY_COPY_ID gives arrival order.
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
    public int holdCount(String callNumber)
    {
        if (Book.isBlank(callNumber))
        {
            return 0;
        }
        String call = callNumber.trim();
        return ChainTools.findAll(holds, h -> h.getCallNumber().equals(call))
            .size();
    }


    /** True if the copy is on the shelf. */
    public boolean isOnShelf(int copyId)
    {
        Book book = findCopy(copyId);
        return book != null && shelf.contains(book);
    }


    /** True if the patron has that copy on loan. */
    public boolean hasBorrowed(String patronId, int copyId)
    {
        Patron patron = findPatron(patronId);
        Book book = findCopy(copyId);
        return patron != null && book != null && patron.hasBorrowed(book);
    }


    /** Copies a patron has out, or -1 if the patron does not exist. */
    public int borrowedCount(String patronId)
    {
        Patron patron = findPatron(patronId);
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
     * Gives a copy to the earliest hold for its title, else shelves it.
     *
     * @return the hold that was filled, or null if the copy was shelved
     */
    private HoldRequest placeCopy(Book book)
    {
        HoldRequest next = earliestHold(book.getCallNumber());
        if (next == null)
        {
            shelf.add(book);
            return null;
        }
        holds.remove(next);
        next.getPatron().borrow(book, today + LOAN_DAYS);
        return next;
    }


    /** Lowest sequence number among holds for a title, or null. */
    private HoldRequest earliestHold(String callNumber)
    {
        HoldRequest best = null;
        for (Object o : ChainTools.snapshot(holds))
        {
            HoldRequest h = (HoldRequest)o;
            if (h.getCallNumber().equals(callNumber)
                && (best == null || h.getSequence() < best.getSequence()))
            {
                best = h;
            }
        }
        return best;
    }


    private Patron findPatron(String patronId)
    {
        if (Book.isBlank(patronId))
        {
            return null;
        }
        String id = patronId.trim();
        return ChainTools.findFirst(patrons, p -> p.getId().equals(id));
    }


    private Book findCopy(int copyId)
    {
        return ChainTools.findFirst(catalog, b -> b.getCopyId() == copyId);
    }


    private boolean titleExists(String callNumber)
    {
        if (Book.isBlank(callNumber))
        {
            return false;
        }
        String call = callNumber.trim();
        return ChainTools
            .findFirst(catalog, b -> b.getCallNumber().equals(call)) != null;
    }
}
