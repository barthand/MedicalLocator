package put.medicallocator.utils;

import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CollectionUtils {

    /**
     * Checks if provided {@link Collection} is null and not empty.
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return (collection != null && collection.size() > 0);
    }

    /**
     * Retrieves the existing integer indexes coming from provided {@link SparseBooleanArray} under which there is
     * certain boolean {@code value} set.
     */
    public static List<Integer> getIndexesFromSparseBooleanArray(SparseBooleanArray array, boolean value) {
        List<Integer> result = new ArrayList<Integer>();
        for (int i=0; i<array.size(); i++) {
            if (array.valueAt(i) == value) {
                result.add(array.keyAt(i));
            }
        }
        return result;
    }

    private CollectionUtils() {
    }
}
