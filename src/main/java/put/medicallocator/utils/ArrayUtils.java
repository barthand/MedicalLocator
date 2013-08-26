package put.medicallocator.utils;

public final class ArrayUtils {

    /**
     * Checks whether the provided {@code array} is empty (i.e is not null and has non-empty length).
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return (array != null && array.length > 0);
    }

    private ArrayUtils() {
    }

}


