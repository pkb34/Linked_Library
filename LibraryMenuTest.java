/**
 * Test class for LibraryMenu, with each input validation, prompt, 
 * and menu action separated into its own test method.
 */
import java.util.Scanner; 

public class LibraryMenuTest extends student.TestCase {
  private Library library;
  
public void setUp() {
        library = new Library();
    }

public void testRun() {
  setIn("0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();
        assertContains(getLog(), "Goodbye.");

  setIn("2\nP001\nAlice\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertEquals(1, library.patronCount());
        assertContains(getLog(), "Goodbye.");

  setIn("7\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertContains(getLog(), "The shelf is empty.");
        assertContains(getLog(), "Goodbye.");
}

  public void testHandle(){
    setIn("1\nClean Code\nRobert Martin\nQA123\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertEquals(1, library.catalogSize());
        assertContains(getLog(), Outcome.SUCCESS.getMessage());

    setIn("2\nP001\nAlice\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertEquals(2, library.patronCount());
        assertContains(getLog(), Outcome.SUCCESS.getMessage());

    library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");

       
        setIn("3\nP001\nQA123\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertEquals(0, library.shelfSize());
        assertEquals(1, library.borrowedCount("P001"));
  }

  public void testHandle_case4_returnBook() {
        
        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");
        library.checkOut("P001", "QA123");

        
        setIn("4\nP001\n1\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertEquals(1, library.shelfSize());
        assertContains(getLog(), Outcome.SUCCESS.getMessage());
    }

    public void testHandle_case5_placeHold() {
       
        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");
        library.addPatron("P002", "Bob");
        library.checkOut("P001", "QA123");

        
        setIn("5\nP002\nQA123\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertEquals(1, library.holdCount("QA123"));
        assertContains(getLog(), Outcome.SUCCESS.getMessage());
    }

    public void testHandle_case6_searchNoMatches() {
        g
        setIn("6\nNonexistent\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertContains(getLog(), "No matches.");
    }

    public void testHandle_case6_searchWithMatches() {
        library.addBook("Clean Code", "Robert Martin", "QA123");

        
        setIn("6\nMartin\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertContains(getLog(), "Clean Code");
    }

    public void testHandle_case7_showShelf() {
        
        setIn("7\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertContains(getLog(), "The shelf is empty.");
    }

    public void testHandle_case8_removeBook() {
        library.addBook("Clean Code", "Robert Martin", "QA123");

       
        setIn("8\n1\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertEquals(0, library.catalogSize());
        assertContains(getLog(), Outcome.SUCCESS.getMessage());
    }

    public void testHandle_case9_removePatron() {
        library.addPatron("P001", "Alice");

       
        setIn("9\nP001\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();

        assertEquals(0, library.patronCount());
        assertContains(getLog(), Outcome.SUCCESS.getMessage());
    }


  public void testShelfEmpty() {
    setIn("7\n0\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        menu.run();
        assertContains(getLog(), "The shelf is empty.");
  }

  public void testReadInt_validDirect() {
        setIn("5\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));
        
        int value = menu.readInt("Choice: ", 0, 9);
        assertEquals(5, value);
        assertContains(getLog(), "Choice: ");
    }

    public void testReadInt_repromptsOnInvalidFormat() {
        setIn("abc\n15\n3\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        int value = menu.readInt("Choice: ", 0, 9);
        assertEquals(3, value);
        assertContains(getLog(), "Enter a whole number from 0 to 9.");
    }


    public void testReadText_validDirect() {
        setIn("Clean Code\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        String text = menu.readText("Title: ");
        assertEquals("Clean Code", text);
    }

    public void testReadText_repromptsOnBlank() {
        setIn("   \nDesign Patterns\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        String text = menu.readText("Title: ");
        assertEquals("Design Patterns", text);
        assertContains(getLog(), "This can't be blank.");
    }

    public void testReadText_repromptsOnTooLong() {
        String longText = "a".repeat(105);
        setIn(longText + "\nShort Title\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        String text = menu.readText("Title: ");
        assertEquals("Short Title", text);
        assertContains(getLog(), "Too long (max 100 characters).");
    }

    
    public void testReadYesNo_trueForY() {
        setIn("y\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        assertTrue(menu.readYesNo("Proceed? (y/n): "));
    }

    public void testReadYesNo_falseForN() {
        setIn("n\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        assertFalse(menu.readYesNo("Proceed? (y/n): "));
    }

    public void testReadYesNo_repromptsOnInvalidInput() {
        setIn("maybe\ny\n");
        LibraryMenu menu = new LibraryMenu(library, new Scanner(systemIn()));

        assertTrue(menu.readYesNo("Proceed? (y/n): "));
        assertContains(getLog(), "Please enter y or n.");
    }
  
public void testCheckOut() {

        library.addBook("Clean Code", "Robert Martin", "QA123");
        library.addPatron("P001", "Alice");
        library.addPatron("P002", "Bob");

        setIn("3\nP001\nQA123\n0\n");
        LibraryMenu menu1 = new LibraryMenu(library, new Scanner(systemIn()));
        menu1.run();
        
        assertEquals(0, library.shelfSize());
        assertEquals(1, library.borrowedCount("P001"));
        assertContains(getLog(), Outcome.SUCCESS.getMessage());


        setIn("3\nP002\nQA123\nn\n0\n");
        LibraryMenu menu2 = new LibraryMenu(library, new Scanner(systemIn()));
        menu2.run();

        assertContains(getLog(), Outcome.NONE_AVAILABLE.getMessage());
        assertContains(getLog(), "Place a hold instead? (y/n): ");
        assertEquals(0, library.holdCount("QA123"));

  
        setIn("3\nP002\nQA123\ny\n0\n");
        LibraryMenu menu3 = new LibraryMenu(library, new Scanner(systemIn()));
        menu3.run();

        assertEquals(1, library.holdCount("QA123"));
        assertContains(getLog(), Outcome.SUCCESS.getMessage());
    }

  public void testShowShelf() {
      
        setIn("7\n0\n");
        LibraryMenu menu1 = new LibraryMenu(library, new Scanner(systemIn()));
        menu1.run();
        assertContains(getLog(), "The shelf is empty.");

        library.addBook("Clean Code", "Robert Martin", "QA123");
        setIn("7\n0\n");
        LibraryMenu menu2 = new LibraryMenu(library, new Scanner(systemIn()));
        menu2.run();
        assertContains(getLog(), "Clean Code");
    }
  
}
