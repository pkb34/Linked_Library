public class LibraryTest extends student.TestCase {
    private Library library;


    public void setUp() {
        library = new Library();
    }


    public void testAddBook() {
      Outcome result = library.addBook("Clean Code", "Robert Martin", "QA123");
        assertEquals(Outcome.SUCCESS, result);
        assertEquals(1, library.catalogSize());
        assertEquals(1, library.shelfSize());

        Outcome result = library.addBook("", "Robert Martin", "QA123");
        assertEquals(Outcome.INVALID_INPUT, result);
        assertEquals(0, library.catalogSize());
    }

    public void testRemoveBook(){
        library.addBook("Clean Code", "Robert Martin", "QA123");
        Outcome result = library.removeBook(1);
        assertEquals(Outcome.SUCCESS, result);
        assertEquals(0, library.catalogSize());
        assertEquals(0, library.shelfSize());

        Outcome result = library.removeBook(99);
        assertEquals(Outcome.BOOK_NOT_FOUND, result);

        
    }


    public void testAddPatron() {
        Outcome result = library.addPatron("P001", "Alice");
        assertEquals(Outcome.SUCCESS, result);
        assertEquals(1, library.patronCount());

        library.addPatron("P001", "Alice");
        Outcome result = library.addPatron("P001", "Bob");
        assertEquals(Outcome.DUPLICATE_ID, result);

        Outcome result = library.addPatron("", "Alice");
        assertEquals(Outcome.INVALID_INPUT, result);
    }


    public void testRemovePatron() {
     library.addPatron("P001", "Alice");
        Outcome result = library.removePatron("P001");
        assertEquals(Outcome.SUCCESS, result);
        assertEquals(0, library.patronCount());

       Outcome result = library.removePatron("P999");
        assertEquals(Outcome.PATRON_NOT_FOUND, result);

        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");
        library.checkOut("P001", "QA123");

        Outcome result = library.removePatron("P001");
        assertEquals(Outcome.HAS_DEPENDENTS, result);
    }

    public void testCheckOut(){
        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");

        Outcome result = library.checkOut("P001", "QA123");
        assertEquals(Outcome.SUCCESS, result);
        assertEquals(0, library.shelfSize());
        assertEquals(1, library.borrowedCount("P001"));

        library.addBook("Clean Code", "Robert Martin", "QA123");
        Outcome result = library.checkOut("P999", "QA123");
        assertEquals(Outcome.PATRON_NOT_FOUND, result);

        library.addPatron("P001", "Alice");
        Outcome result = library.checkOut("P001", "NONEXISTENT");
        assertEquals(Outcome.BOOK_NOT_FOUND, result);

        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");
        library.addPatron("P002", "Bob");
        library.checkOut("P001", "QA123"); 
        
        Outcome result = library.checkOut("P002", "QA123");
        assertEquals(Outcome.NONE_AVAILABLE, result);
    }

    public void testReturnBook(){
        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");
        library.checkOut("P001", "QA123");

        Outcome result = library.returnBook("P001", 1);
        assertEquals(Outcome.SUCCESS, result);
        assertEquals(1, library.shelfSize());

        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");
        library.addPatron("P002", "Bob");
        library.checkOut("P001", "QA123");
        library.placeHold("P002", "QA123");

        Outcome result = library.returnBook("P001", 1);
        assertEquals(Outcome.ASSIGNED_TO_HOLD, result);
        assertEquals(0, library.shelfSize());
        assertEquals(1, library.borrowedCount("P002"));

        library.addBook("Clean Code", "Robert Martin", "QA123");
        Outcome result = library.returnBook("P999", 1);
        assertEquals(Outcome.PATRON_NOT_FOUND, result);

        library.addPatron("P001", "Alice");
        Outcome result = library.returnBook("P001", 999);
        assertEquals(Outcome.BOOK_NOT_FOUND, result);

        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");

        Outcome result = library.returnBook("P001", 1);
        assertEquals(Outcome.NOT_BORROWED, result);
    }

    public void testPlaceHold(){
        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");
        library.addPatron("P002", "Bob");
        library.checkOut("P001", "QA123"); 

        Outcome result = library.placeHold("P002", "QA123");
        assertEquals(Outcome.SUCCESS, result);
        assertEquals(1, library.holdCount("QA123"));

        library.addBook("Clean Code", "Robert Martin", "QA123"); 
        library.addPatron("P001", "Alice");

      
        Outcome result = library.placeHold("P001", "QA123");
        assertEquals(Outcome.BOOK_AVAILABLE, result);

        library.addBook("Clean Code", "Robert Martin", "QA123"); 
        library.addPatron("P001", "Alice");

        
        Outcome result = library.placeHold("P001", "QA123");
        assertEquals(Outcome.BOOK_AVAILABLE, result);
    }

    public void testIsOnShelf(){
        library.addBook("Clean Code", "Robert Martin", "QA123");
        assertTrue(library.isOnShelf(1));

        library.addPatron("P001", "Alice");
        library.checkOut("P001", "QA123");
        assertFalse(library.isOnShelf(1));
    }
}


