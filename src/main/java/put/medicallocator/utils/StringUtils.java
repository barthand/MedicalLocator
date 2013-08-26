package put.medicallocator.utils;

public final class StringUtils {

    /**
     * Checks whether the provided {@link String} is empty (i.e is either null or has zero-based length).
     */
    public static boolean isEmpty(String s) {
        return !(s != null && s.length() > 0);
    }

    /**
     * Joins {@link String}s elements from the {@code array} using provided {@code separator}.
     */
    public static String join(String[] array, String separator) {
        if (ArrayUtils.isNotEmpty(array)) {
            final StringBuilder builder = new StringBuilder(array[0]);
            for (int i = 1; i < array.length; i++) {
                builder.append(separator).append(array[i]);
            }
            return builder.toString();
        }
        return null;
    }

    private StringUtils() {
    }

}
