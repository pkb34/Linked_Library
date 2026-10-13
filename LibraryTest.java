public class LibraryTest extends student.TestCase {
    private Library library;


    public void setUp() {
        library = new Library();
    }


    public void testAddBookAndCatalogSize() {
        Outcome result = library.addBook("Clean Code", "Robert Martin", "QA123");
        assertEquals(Outcome.SUCCESS, result);
        assertEquals(1, library.catalogSize());
        assertEquals(1, library.shelfSize());
    }

t
    public void testAddDuplicatePatronId() {
        library.addPatron("P001", "Alice");
        Outcome result = library.addPatron("P001", "Bob");
        assertEquals(Outcome.DUPLICATE_ID, result);
    }


    public void testCheckOutAndReturn() {
        library.addBook("Java Programming", "John Doe", "JB456");
        library.addPatron("P002", "Charlie");

       
        Outcome checkoutResult = library.checkOut("P002", "JB456");
        assertEquals(Outcome.SUCCESS, checkoutResult);
        assertEquals(0, library.shelfSize());
        assertEquals(1, library.borrowedCount("P002"));

    
        Outcome returnResult = library.returnBook("P002", 1);
        assertEquals(Outcome.SUCCESS, returnResult);
        assertEquals(1, library.shelfSize());
    }
}


