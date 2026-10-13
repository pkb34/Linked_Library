package LinkedLibrary;

import java.util.Scanner;

/** Starts the Linked Library program. */
public class LibraryApp
{
    public static void main(String[] args)
    {
        new LibraryMenu(new Library(), new Scanner(System.in)).run();
    }
}
