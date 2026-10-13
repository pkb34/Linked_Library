import java.util.Scanner;

/**
 * Console interface. Reads and validates raw input, calls Library, and prints
 * what comes back. It never changes library state directly.
 */
public class LibraryMenu
{
    /** Longest text the menu accepts. */
    public static final int MAX_TEXT = 100;
    private static final int QUIT = 0;
    private static final int LAST_OPTION = 13;

    private final Library library;
    private final Scanner in;

    /**
     * @throws IllegalArgumentException
     *             if library or in is null
     */
    public LibraryMenu(Library library, Scanner in)
    {
        if (library == null || in == null)
        {
            throw new IllegalArgumentException("Library and Scanner required");
        }
        this.library = library;
        this.in = in;
    }


    /** Main loop. Returns when the user quits or input runs out. */
    public void run()
    {
        while (true)
        {
            printMenu();
            Integer choice = readIntOrNull("Choice: ", QUIT, LAST_OPTION);
            if (choice == null || choice == QUIT)
            {
                System.out.println("Goodbye.");
                return;
            }
            handle(choice);
        }
    }


    private void printMenu()
    {
        System.out.println();
        System.out.println("Day " + library.getToday());
        System.out.println(" 1) Add book       2) Add patron     3) Check out");
        System.out.println(" 4) Return         5) Place hold     6) Search");
        System.out.println(" 7) Show shelf     8) Sorted shelf   9) Undo");
        System.out.println("10) Remove book   11) Remove patron");
        System.out.println("12) Advance days  13) Overdue report  0) Quit");
    }


    private void handle(int choice)
    {
        switch (choice)
        {
            case 1:
                show(
                    library.addBook(
                        readText("Title: "),
                        readText("Author: "),
                        readText("Call number: ")));
                break;
            case 2:
                show(
                    library.addPatron(
                        readText("Patron ID: "),
                        readText("Name: ")));
                break;
            case 3:
                checkOut();
                break;
            case 4:
                show(
                    library.returnBook(
                        readText("Patron ID: "),
                        readInt("Copy #: ", 1, Integer.MAX_VALUE)));
                break;
            case 5:
                show(
                    library.placeHold(
                        readText("Patron ID: "),
                        readText("Call number: ")));
                break;
            case 6:
                printAll(
                    ChainTools.snapshot(library.search(readText("Search: "))),
                    "No matches.");
                break;
            case 7:
                printAll(
                    library.sortedShelf(Book.BY_COPY_ID),
                    "The shelf is empty.");
                break;
            case 8:
                sortedShelf();
                break;
            case 9:
                show(library.undo());
                break;
            case 10:
                show(
                    library
                        .removeBook(readInt("Copy #: ", 1, Integer.MAX_VALUE)));
                break;
            case 11:
                show(library.removePatron(readText("Patron ID: ")));
                break;
            case 12:
                show(library.advanceDays(readInt("Days: ", 1, 365)));
                break;
            case 13:
                overdueReport();
                break;
            default:
                break;
        }
    }


    /** Checkout, offering a hold if every copy is out. */
    private void checkOut()
    {
        String id = readText("Patron ID: ");
        String call = readText("Call number: ");
        Outcome result = library.checkOut(id, call);
        show(result);
        if (result == Outcome.NONE_AVAILABLE
            && readYesNo("Place a hold instead? (y/n): "))
        {
            show(library.placeHold(id, call));
        }
    }


    private void sortedShelf()
    {
        int by = readInt("Sort by 1) title 2) author 3) call number: ", 1, 3);
        if (by == 1)
        {
            printAll(library.sortedShelf(Book.BY_TITLE), "The shelf is empty.");
        }
        else if (by == 2)
        {
            printAll(
                library.sortedShelf(Book.BY_AUTHOR),
                "The shelf is empty.");
        }
        else
        {
            printAll(
                library.sortedShelf(Book.BY_CALL_NUMBER),
                "The shelf is empty.");
        }
    }


    private void overdueReport()
    {
        Loan[] late = library.overdueLoans();
        if (late.length == 0)
        {
            System.out.println("Nothing is overdue.");
        }
        for (Loan loan : late)
        {
            System.out.println(
                loan + " (" + loan.daysLate(library.getToday())
                    + " days late)");
        }
    }


    private void printAll(Object[] items, String ifEmpty)
    {
        if (items.length == 0)
        {
            System.out.println(ifEmpty);
        }
        for (Object item : items)
        {
            System.out.println(item);
        }
    }


    private void show(Outcome outcome)
    {
        System.out.println(outcome.getMessage());
    }


    /**
     * Re-prompts until the line is a whole number in [min, max].
     *
     * @throws IllegalStateException
     *             if input runs out first
     */
    public int readInt(String prompt, int min, int max)
    {
        Integer value = readIntOrNull(prompt, min, max);
        if (value == null)
        {
            throw new IllegalStateException("No more input");
        }
        return value;
    }


    /**
     * Re-prompts until the text is non-blank and at most MAX_TEXT characters.
     * Returns it trimmed.
     *
     * @throws IllegalStateException
     *             if input runs out first
     */
    public String readText(String prompt)
    {
        while (true)
        {
            String line = nextLine(prompt);
            if (line.isEmpty())
            {
                System.out.println("This can't be blank.");
            }
            else if (line.length() > MAX_TEXT)
            {
                System.out
                    .println("Too long (max " + MAX_TEXT + " characters).");
            }
            else
            {
                return line;
            }
        }
    }


    /**
     * Re-prompts until the answer is y or n (any case).
     *
     * @throws IllegalStateException
     *             if input runs out first
     */
    public boolean readYesNo(String prompt)
    {
        while (true)
        {
            String line = nextLine(prompt).toLowerCase();
            if (line.equals("y"))
            {
                return true;
            }
            if (line.equals("n"))
            {
                return false;
            }
            System.out.println("Please enter y or n.");
        }
    }


    /** Like readInt, but returns null when input runs out. */
    private Integer readIntOrNull(String prompt, int min, int max)
    {
        while (true)
        {
            System.out.print(prompt);
            if (!in.hasNextLine())
            {
                return null;
            }
            String line = in.nextLine().trim();
            try
            {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max)
                {
                    return value;
                }
            }
            catch (NumberFormatException e)
            {
                // not a number; fall through to the message below
            }
            System.out.println(
                "Enter a whole number from " + min + " to " + max + ".");
        }
    }


    private String nextLine(String prompt)
    {
        System.out.print(prompt);
        if (!in.hasNextLine())
        {
            throw new IllegalStateException("No more input");
        }
        return in.nextLine().trim();
    }
}
