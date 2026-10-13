package LinkedLibrary;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Generic helpers that work for any LinkedChain<T>, using only its public
 * methods (size, toArray, add). Every app class goes through these instead of
 * writing one search loop per element type. One snapshot (toArray) per call
 * keeps every search O(n). Calling get(i) in a loop would be O(n^2) because
 * each get walks from the head.
 */
public final class ChainTools
{

    private ChainTools()
    {
    }


    /**
     * First element that passes the test, or null if none does.
     *
     * @throws IllegalArgumentException
     *             if chain or test is null
     */
    public static <
        T> T findFirst(LinkedChain<T> chain, Predicate<? super T> test)
    {
        Object[] items = snapshot(chain);
        requireNonNull(test);
        for (Object o : items)
        {
            @SuppressWarnings("unchecked")
            T item = (T)o;
            if (test.test(item))
            {
                return item;
            }
        }
        return null;
    }


    /**
     * A new chain holding every element that passes the test. The original
     * chain is not changed.
     *
     * @throws IllegalArgumentException
     *             if chain or test is null
     */
    public static <T> LinkedChain<T> findAll(
        LinkedChain<T> chain,
        Predicate<? super T> test)
    {
        Object[] items = snapshot(chain);
        requireNonNull(test);
        LinkedChain<T> result = new LinkedChain<T>();
        for (Object o : items)
        {
            @SuppressWarnings("unchecked")
            T item = (T)o;
            if (test.test(item))
            {
                result.add(item);
            }
        }
        return result;
    }


    /**
     * The chain's elements copied into an array. Returned as Object[] because
     * the chain cannot build a real T[]; callers cast each element.
     *
     * @throws IllegalArgumentException
     *             if chain is null
     */
    public static <T> Object[] snapshot(LinkedChain<T> chain)
    {
        requireNonNull(chain);
        @SuppressWarnings("unchecked")
        T[] values = (T[])new Object[chain.size()];
        return chain.toArray(values);
    }


    /**
     * The chain's elements sorted by cmp (insertion sort, stable). The chain
     * itself is not changed.
     *
     * @throws IllegalArgumentException
     *             if chain or cmp is null
     */
    public static <T> Object[] sortedSnapshot(
        LinkedChain<T> chain,
        Comparator<? super T> cmp)
    {
        Object[] items = snapshot(chain);
        requireNonNull(cmp);
        for (int i = 1; i < items.length; i++)
        {
            Object key = items[i];
            int j = i - 1;
            while (j >= 0 && compare(cmp, items[j], key) > 0)
            {
                items[j + 1] = items[j];
                j--;
            }
            items[j + 1] = key;
        }
        return items;
    }


    @SuppressWarnings("unchecked")
    private static <
        T> int compare(Comparator<? super T> cmp, Object a, Object b)
    {
        return cmp.compare((T)a, (T)b);
    }


    private static void requireNonNull(Object o)
    {
        if (o == null)
        {
            throw new IllegalArgumentException("Argument cannot be null");
        }
    }
}
