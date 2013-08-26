package put.medicallocator.ui.utils;

import android.util.SparseIntArray;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;

import java.util.List;

/**
 * {@link SectionIndexer} based on the alphabet. Implementation come from the {@link AlphabetIndexer},
 * but it is adjusted to accept arrays as a source.
 */
public class AlphabetArrayIndexer<T> implements SectionIndexer {

    public interface IndexedValueReturner<T> {
        String indexedValue(T value);
    }

    /**
     * Cursor that is used by the adapter of the list view.
     */
    protected List<T> dataList;

    /**
     * Retrieves the indexed value from the Type associated with input data.
     */
    protected final IndexedValueReturner<T> valueReturner;

    /**
     * The string of characters that make up the indexing sections.
     */
    protected CharSequence alphabet;

    /**
     * Cached length of the alphabet array.
     */
    private int alphabetLength;

    /**
     * This contains a cache of the computed indices so far. It will get reset whenever
     * the dataset changes or the cursor changes.
     */
    private SparseIntArray alphaMap;

    /**
     * Use a collator to compare strings in a localized manner.
     */
    private java.text.Collator collator;

    /**
     * The section array converted from the alphabet string.
     */
    private String[] alphabetArray;

    /**
     * Constructs the indexer.
     * @param list the list containing the data set
     * @param returner to avoid reflections, retrieves the indexed column from the object itself.
     * @param alphabet string containing the alphabet, with space as the first character.
     *        For example, use the string " ABCDEFGHIJKLMNOPQRSTUVWXYZ" for English indexing.
     *        The characters must be uppercase and be sorted in ascii/unicode order. Basically
     *        characters in the alphabet will show up as preview letters.
     */
    public AlphabetArrayIndexer(List<T> list, IndexedValueReturner<T> returner, CharSequence alphabet) {
        dataList = list;
        valueReturner = returner;
        this.alphabet = alphabet;
        alphabetLength = alphabet.length();
        alphabetArray = new String[alphabetLength];
        for (int i = 0; i < alphabetLength; i++) {
            alphabetArray[i] = Character.toString(this.alphabet.charAt(i));
        }
        alphaMap = new SparseIntArray(alphabetLength);
        // Get a Collator for the current locale for string comparisons.
        collator = java.text.Collator.getInstance();
        collator.setStrength(java.text.Collator.PRIMARY);
    }

    /**
     * Returns the section array constructed from the alphabet provided in the constructor.
     * @return the section array
     */
    public Object[] getSections() {
        return alphabetArray;
    }

    /**
     * Default implementation compares the first character of word with letter.
     */
    protected int compare(String word, String letter) {
        final String firstLetter;
        if (word.length() == 0) {
            firstLetter = " ";
        } else {
            firstLetter = word.substring(0, 1);
        }

        return collator.compare(firstLetter, letter);
    }

    /**
     * Performs a binary search or cache lookup to find the first row that
     * matches a given section's starting letter.
     * @param sectionIndex the section to search for
     * @return the row index of the first occurrence, or the nearest next letter.
     * For instance, if searching for "T" and no "T" is found, then the first
     * row starting with "U" or any higher letter is returned. If there is no
     * data following "T" at all, then the list size is returned.
     */
    public int getPositionForSection(int sectionIndex) {
        final SparseIntArray alphaMap = this.alphaMap;
        final List<T> data = dataList;

        if (data == null || alphabet == null) {
            return 0;
        }

        // Check bounds
        if (sectionIndex <= 0) {
            return 0;
        }
        if (sectionIndex >= alphabetLength) {
            sectionIndex = alphabetLength - 1;
        }

        int count = data.size();
        int start = 0;
        int end = count;
        int pos;

        char letter = alphabet.charAt(sectionIndex);
        String targetLetter = Character.toString(letter);
        int key = letter;
        // Check map
        if (Integer.MIN_VALUE != (pos = alphaMap.get(key, Integer.MIN_VALUE))) {
            // Is it approximate? Using negative value to indicate that it's
            // an approximation and positive value when it is the accurate
            // position.
            if (pos < 0) {
                pos = -pos;
                end = pos;
            } else {
                // Not approximate, this is the confirmed start of section, return it
                return pos;
            }
        }

        // Do we have the position of the previous section?
        if (sectionIndex > 0) {
            int prevLetter =
                    alphabet.charAt(sectionIndex - 1);
            int prevLetterPos = alphaMap.get(prevLetter, Integer.MIN_VALUE);
            if (prevLetterPos != Integer.MIN_VALUE) {
                start = Math.abs(prevLetterPos);
            }
        }

        // Now that we have a possibly optimized start and end, let's binary search

        pos = (end + start) / 2;

        while (pos < end) {
            // Get letter at pos
            String curName = valueReturner.indexedValue(data.get(pos));
            if (curName == null) {
                if (pos == 0) {
                    break;
                } else {
                    pos--;
                    continue;
                }
            }
            int diff = compare(curName, targetLetter);
            if (diff != 0) {
                if (diff < 0) {
                    start = pos + 1;
                    if (start >= count) {
                        pos = count;
                        break;
                    }
                } else {
                    end = pos;
                }
            } else {
                // They're the same, but that doesn't mean it's the start
                if (start == pos) {
                    // This is it
                    break;
                } else {
                    // Need to go further lower to find the starting row
                    end = pos;
                }
            }
            pos = (start + end) / 2;
        }
        alphaMap.put(key, pos);
        return pos;
    }

    /**
     * Returns the section index for a given position in the list by querying the item
     * and comparing it with all items in the section array.
     */
    public int getSectionForPosition(int position) {
        String curName = valueReturner.indexedValue(dataList.get(position));
        // Linear search, as there are only a few items in the section index
        // Could speed this up later if it actually gets used.
        for (int i = 0; i < alphabetLength; i++) {
            char letter = alphabet.charAt(i);
            String targetLetter = Character.toString(letter);
            if (compare(curName, targetLetter) == 0) {
                return i;
            }
        }
        return 0; // Don't recognize the letter - falls under zero'th section
    }

}
