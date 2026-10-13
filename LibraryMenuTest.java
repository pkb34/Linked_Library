package LinkedLibrary;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Tests for LibraryMenu and LibraryApp, using scripted input. */
public class LibraryMenuTest
{
    private final PrintStream realOut = System.out;
    private final InputStream realIn = System.in;
    private ByteArrayOutputStream out;
    private Library lib;

    @Before
    public void setUp()
    {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        lib = new Library();
    }


    @After
    public void tearDown()
    {
        System.setOut(realOut);
        System.setIn(realIn);
    }


    private LibraryMenu menu(String input)
    {
        return new LibraryMenu(lib, new Scanner(input));
    }


    private String output()
    {
        return out.toString();
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNull()
    {
        new LibraryMenu(null, new Scanner(""));
    }

    // ----- readInt -----


    @Test
    public void readIntAcceptsNumberInRange()
    {
        assertEquals(7, menu(" 7 \n").readInt("> ", 0, 9));
    }


    @Test
    public void readIntRepromptsOnTextAndOutOfRange()
    {
        assertEquals(3, menu("abc\n99\n-1\n\n3\n").readInt("> ", 0, 9));
        assertTrue(output().contains("Enter a whole number from 0 to 9."));
    }


    @Test(expected = IllegalStateException.class)
    public void readIntWithNoInputThrows()
    {
        menu("").readInt("> ", 0, 9);
    }

    // ----- readText -----


    @Test
    public void readTextTrims()
    {
        assertEquals("Dune", menu("  Dune  \n").readText("> "));
    }


    @Test
    public void readTextRepromptsOnBlankAndTooLong()
    {
        String tooLong = "x".repeat(LibraryMenu.MAX_TEXT + 1);
        assertEquals("ok", menu("   \n" + tooLong + "\nok\n").readText("> "));
        assertTrue(output().contains("This can't be blank."));
        assertTrue(output().contains("Too long"));
    }


    @Test
    public void readTextKeepsSpecialCharacters()
    {
        assertEquals(
            "\"Quotes\", commas & é",
            menu("\"Quotes\", commas & é\n").readText("> "));
    }

    // ----- readYesNo -----


    @Test
    public void readYesNoAcceptsEitherCase()
    {
        assertTrue(menu("Y\n").readYesNo("> "));
        assertFalse(menu("n\n").readYesNo("> "));
    }


    @Test
    public void readYesNoRepromptsOnOtherText()
    {
        assertTrue(menu("maybe\n\ny\n").readYesNo("> "));
        assertTrue(output().contains("Please enter y or n."));
    }

    // ----- run -----


    @Test
    public void runMvpPath()
    {
        String script = String.join(
            "\n",
            "2",
            "Ana",
            "2",
            "Bo",
            "1",
            "The Hobbit",
            "Tolkien",
            "3",
            "Ana",
            "The Hobbit",
            "3",
            "Bo",
            "the hobbit",
            "y",
            "4",
            "Ana",
            "1",
            "6",
            "hob",
            "7",
            "8",
            "1",
            "9",
            "12",
            "20",
            "13",
            "0",
            "");
        menu(script).run();
        String text = output();
        assertTrue(text.contains("Day 21"));
        assertTrue(text.contains("- Ana, due day 15 (6 days late)"));
        assertTrue(text.contains("No copies of that title are on the shelf."));
        assertTrue(text.contains("The copy went to the next patron on hold."));
        assertTrue(text.contains("#1 The Hobbit by Tolkien"));
        assertTrue(text.contains("Goodbye."));
        assertTrue(lib.hasBorrowed("Ana", 1));   // the return was undone
    }


    @Test
    public void runSurvivesBadInput()
    {
        String script = String.join(
            "\n",
            "hello",
            "42",
            "-1",
            "3",
            "Nobody",
            "The Hobbit",
            "4",
            "Ana",
            "abc",
            "1",
            "9",
            "10",
            "5",
            "11",
            "Nobody",
            "8",
            "7",
            "2",
            "7",
            "12",
            "0",
            "400",
            "3",
            "13",
            "0",
            "");
        menu(script).run();
        String text = output();
        assertTrue(text.contains("Enter a whole number from 0 to 13."));
        assertTrue(text.contains("Enter a whole number from 1 to 365."));
        assertTrue(text.contains("Nothing is overdue."));
        assertTrue(text.contains("Enter a whole number from 1 to 2."));
        assertTrue(text.contains("Patron not found."));
        assertTrue(text.contains("Nothing to undo."));
        assertTrue(text.contains("Book not found."));
        assertTrue(text.contains("The shelf is empty."));
        assertTrue(text.contains("Goodbye."));
    }


    @Test
    public void runEndsCleanlyWhenInputRunsOut()
    {
        menu("").run();
        assertTrue(output().contains("Goodbye."));
    }

    // ----- LibraryApp -----


    @Test
    public void appMainStartsAndQuits()
    {
        System.setIn(new ByteArrayInputStream("0\n".getBytes()));
        LibraryApp.main(new String[0]);
        assertTrue(output().contains("Goodbye."));
    }


    @Test
    public void appMainSurvivesGarbageThenEndOfInput()
    {
        System.setIn(new ByteArrayInputStream("garbage\n".getBytes()));
        LibraryApp.main(null);
        assertTrue(output().contains("Enter a whole number"));
    }
}
