package LinkedLibrary;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ChainTools. The same helpers are run on two unrelated element types
 * (String and Book) to show they are generic.
 */
public class ChainToolsTest
{
    private LinkedChain<String> words;
    private LinkedChain<Book> books;
    private LinkedChain<String> empty;

    @Before
    public void setUp()
    {
        words = new LinkedChain<String>();
        words.addToEnd("pear");
        words.addToEnd("apple");
        words.addToEnd("plum");
        books = new LinkedChain<Book>();
        books.addToEnd(new Book("Zeta", "Adams", 1));
        books.addToEnd(new Book("Alpha", "Zed", 2));
        empty = new LinkedChain<String>();
    }


    @Test
    public void findFirstWorksForTwoTypes()
    {
        assertEquals(
            "pear",
            ChainTools.findFirst(words, w -> w.startsWith("p")));
        assertEquals(
            2,
            ChainTools.findFirst(books, b -> b.getTitle().equals("Alpha"))
                .getCopyNumber());
    }


    @Test
    public void findFirstReturnsNullWhenMissingOrEmpty()
    {
        assertNull(ChainTools.findFirst(words, w -> w.equals("kiwi")));
        assertNull(ChainTools.findFirst(empty, w -> true));
    }


    @Test(expected = IllegalArgumentException.class)
    public void findFirstRejectsNullChain()
    {
        ChainTools.findFirst((LinkedChain<String>)null, w -> true);
    }


    @Test
    public void findAllKeepsMatchesAndLeavesOriginal()
    {
        LinkedChain<String> p =
            ChainTools.findAll(words, w -> w.startsWith("p"));
        assertEquals(2, p.size());
        assertTrue(p.contains("pear"));
        assertTrue(p.contains("plum"));
        assertEquals(3, words.size());
    }


    @Test
    public void findAllOnEmptyChainIsEmpty()
    {
        assertTrue(ChainTools.findAll(empty, w -> true).isEmpty());
    }


    @Test(expected = IllegalArgumentException.class)
    public void findAllRejectsNullTest()
    {
        ChainTools.findAll(words, null);
    }


    @Test
    public void snapshotCopiesEveryElement()
    {
        Object[] items = ChainTools.snapshot(words);
        assertArrayEquals(new Object[] { "pear", "apple", "plum" }, items);
        assertEquals(0, ChainTools.snapshot(empty).length);
    }


    @Test(expected = IllegalArgumentException.class)
    public void snapshotRejectsNull()
    {
        ChainTools.snapshot(null);
    }


    @Test
    public void sortedSnapshotSortsWithoutChangingChain()
    {
        Object[] sorted = ChainTools.sortedSnapshot(words, String::compareTo);
        assertArrayEquals(new Object[] { "apple", "pear", "plum" }, sorted);
        assertEquals("[pear, apple, plum]", words.toString());
        Object[] byTitle = ChainTools.sortedSnapshot(books, Book.BY_TITLE);
        assertEquals("Alpha", ((Book)byTitle[0]).getTitle());
    }


    @Test
    public void sortedSnapshotOfEmptyChainIsEmpty()
    {
        assertEquals(
            0,
            ChainTools.sortedSnapshot(empty, String::compareTo).length);
    }


    @Test(expected = IllegalArgumentException.class)
    public void sortedSnapshotRejectsNullComparator()
    {
        ChainTools.sortedSnapshot(words, null);
    }
}
