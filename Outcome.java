package LinkedLibrary;

/**
 * The result of a Library operation. Library returns these instead of printing,
 * and LibraryMenu shows the message to the user.
 */
public enum Outcome
{
    SUCCESS("Done."),
    ASSIGNED_TO_HOLD("Done. The copy went to the next patron on hold."),
    INVALID_INPUT("That input was blank or invalid."),
    PATRON_NOT_FOUND("Patron not found."),
    BOOK_NOT_FOUND("Book not found."),
    NONE_AVAILABLE("No copies of that title are on the shelf."),
    NOT_BORROWED("That patron did not borrow that copy."),
    BOOK_AVAILABLE("A copy is on the shelf. Check it out instead."),
    ALREADY_ON_HOLD("Already on the waiting list."),
    DUPLICATE_NAME("A patron with that name already exists."),
    HAS_DEPENDENTS("Cannot remove: there are checkouts or holds."),
    NOTHING_TO_UNDO("Nothing to undo."),
    UNDO_INVALID("That action can no longer be undone, so it was skipped.");

    private final String message;

    Outcome(String message)
    {
        this.message = message;
    }


    public String getMessage()
    {
        return message;
    }
}
