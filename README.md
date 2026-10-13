# Linked_Library
## Reflection

### What changed from our specification?

- **Undo was not built yet.** The spec had `Action`, `ActionType`, a `history` chain capped at 10 actions, and `Library.undo()`. The prototype has none of these implemented yet.
- **Sorted view was not built yet.** The spec had `Book.BY_TITLE`, `BY_AUTHOR`, `BY_CALL_NUMBER` and `Library.sortedShelf(Comparator)`. The prototype only has "Show shelf" (`shelfReport()`) right now, which prints copies in the order the chain holds them.
- **Hold order uses sequence numbers.** The spec said holds are added at the tail and taken from the front of the `holds` chain. We gave `HoldRequest` a sequence number (`nextHoldNumber`), and `Library.earliestHold` picks the lowest number for a title.
- **Blank input is now caught in two places.** The spec said `LibraryMenu.readText` re-prompts and the element constructors throw as a backstop. `Library.addBook` and `Library.addPatron` now also check for blank text and return `INVALID_INPUT`, so `Library` is safe when the menu is not in front of it (for example in tests)
- **New `Outcome` codes.** We added `SUCCESS`, `INVALID_INPUT`, `BOOK_NOT_FOUND` and `ASSIGNED_TO_HOLD`. The spec had no result for "no such copy or call number" or for "a return was auto-assigned to a waiting patron".
- **`addBook` can assign the copy directly.** The spec said a new copy goes to the catalog and the shelf. Now, if someone is waiting for that title, the new copy goes straight to the first waiting patron. This keeps one rule true: a title never has both a shelved copy and a waiting hold.
- **`removeBook` is simpler.** The spec also refused removing the last copy of a held title. We dropped that rule because a title only has holds when no copy is on the shelf, and `removeBook` only removes shelved copies, so it can never apply. Checked-out copies still return `HAS_DEPENDENTS`.
- **Extra read-only methods in `Library`.** We added `holdCount`, `isOnShelf`, `borrowedCount`, `catalogSize`, `shelfSize`, `patronCount` and `shelfReport`, so the menu and tests can check state without touching the chains. The spec's `holdsFor` was not built, and `holdCount` replaced it.
- **"Nothing to remove" message.** The spec said removing from an empty chain shows "Nothing to remove". `Library` now reports `BOOK_NOT_FOUND` or `PATRON_NOT_FOUND` through `Outcome` instead. 
- **Test plan.** The spec listed `LinkedChain` tests, "same tests on `LinkedChain<String>` and `LinkedChain<Book>`", `readInt`/`readText` tests, a stress test and undo tests. So far we only have `LibraryTest`, which tests `Library`

### What changed in the LinkedChain?


- **Unchanged:** We used the provided `LinkedChain.java` unchanged. `Library` only calls `add`, `remove(T)`, `contains`, `get(int)`, `size` and `isEmpty`. We did not need the methods the spec planned to add (`tail`, `addFirst`, `addLast`, `removeFirst`, `filter`, `sortedCopy`, `Iterator`). `Library` has its own private helpers `findFirst` and `findAll` that walk the chain with `size()` and `get(i)`.

One `LinkedChain<T>` class stores three element types so far: `Book` (`catalog`, `shelf` and each patron's borrowed list), `Patron` (`patrons`) and `HoldRequest` (`holds`). `Action` will be the fourth when undo is built. All rules (checkout, hold assignment, duplicate detection) are in `Library`, not in the chain.

### What happened to our unknowns?

1. **Keeping hold queues consistent when a patron or book is removed: figured out.** We refuse the removal instead of cascading. `removePatron` returns `HAS_DEPENDENTS` if the patron has borrowed copies or a hold. `removeBook` only removes shelved copies. Because holds only exist when no copy is shelved, no hold can point at something that was removed. 
2. **Testing reuse across four types: partly open.** `LibraryTest` pushes `Book`, `Patron` and `HoldRequest` through the same chain class. We do not have a direct test running the same cases on `LinkedChain<String>` and `LinkedChain<Book>` yet, and `Action` is not built, so we have only shown three types.
3. **Undo (our biggest risk): still open.** We have not started it. We think the sequence numbers on `HoldRequest` will help: undoing a return that was auto-assigned means putting the hold back with its original number, so it returns to its old place in line. We have not checked this yet.

### What doesn't work yet?

- **Undo** (`Action`, `ActionType`, history, menu option). This is an MVP feature. It also leaves these scope bad-input cases unhandled: undo with empty history, undo of an action that is no longer valid, and a consistent history after rapid checkout and return.
- **Sorted view** by title, author or call number. This is also an MVP feature.
- **Missing tests:** menu tests (`readInt`, `readText`, `readYesNo`), direct `LinkedChain` tests (empty chain, null, bad index, removing something absent), tests for `Book`, `Patron` and `HoldRequest`, the "many identical copies, remove one" case, and a stress test.
- **`LibraryTest` status:** Complies and paasses.
- **Search and lookup speed:** `findFirst` and `findAll` call `get(i)` inside a loop, which is O(n) per call, so each scan is O(n²). The spec assumed O(n), so `checkOut`, `returnBook`, `placeHold` and `search` are slower than the spec's Big-O. 
- **End of input:** `LibraryMenu` uses `Scanner.nextLine()`, which throws `NoSuchElementException` if the input ends (Ctrl-D). This was not one of our scope cases, but it can crash the program.

## Next Steps

### What is left to finish the MVP (in order)

1. Get `LibraryTest` compiling and passing, then add a normal and a bad-input test for every public method.
2. Build undo: `Action` and `ActionType`, a `history` chain capped at 10, `Library.undo()` for add, remove, checkout and return, the `NOTHING_TO_UNDO` and `UNDO_INVALID` outcomes, and a menu option.
3. Build the sorted view: `Book` comparators, `Library.sortedShelf`, and a menu option.
4. Replace the `get(i)` loops with a one-pass traversal so the methods match the spec's Big-O.
5. Add direct `LinkedChain` tests and a stress test for the rule "every copy is in exactly one place".

### Stretch goals

- **Committing to:** [`Iterator<T>`, because it fixes the O(n²) scans; due dates and overdue report only if the MVP is done by [date].]
- **Dropping:** [Patron login with librarian and patron roles, because it is the biggest change and the MVP is not finished.]

### New tests we will need

- **Undo:** each action type, empty history, an action that is no longer valid, and undoing an auto-assigned return so the hold is restored in its old place.
- **Sorted view:** each sort key, original order unchanged, ties kept in the same order, and an empty shelf.
- **`LinkedChain` reuse:** the same tests on `LinkedChain<String>` and `LinkedChain<Book>`, plus null, bad index, remove-absent and empty-chain cases.
- **Other:** `removePatron` when the patron has a hold, many identical copies with one removed, and menu input (`"abc"`, `"99"`, blank, 200 characters).


## Attribution

| File | Who worked on it | How |
|---|---|---|
| `LinkedChain.java` | Provided |
| `Library.java` | [name] | [wrote / revised / reviewed / tested] |
| `LibraryMenu.java` | [name] | [wrote / revised / reviewed / tested] |
| `LibraryMenuTest.java` | [name] | [wrote / revised / reviewed / tested] |
| `LibraryApp.java` | [name] | [wrote / revised / reviewed / tested] |
| `LibraryTest.java` | [name] | [wrote / revised / reviewed / tested] |
