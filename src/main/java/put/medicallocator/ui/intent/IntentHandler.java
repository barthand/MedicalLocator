package put.medicallocator.ui.intent;

import android.content.Intent;

/**
 * Generic interface describing contract for components which may process {@link Intent}s.
 */
public interface IntentHandler {

    /**
     * Processes provided {@link Intent}. This shouldn't be invoked if {@link #supports(Intent)} returns false.
     * Implementations of this method may perform this check internally as well, just to be on the safe side.
     */
    void process(Intent intent);

    /**
     * Indicates whether this {@link IntentHandler} supports provided {@link Intent}.
     */
    boolean supports(Intent intent);

    /**
     * Invoke this if you want this {@link IntentHandler} to retain its state.
     * @return Any object holding the state of this {@link IntentHandler} or null if none.
     */
    Object retainState();

    /**
     * Makes this {@link IntentHandler} to restore its state. Provided object must be the same as the one
     * returned by {@link #retainState()}.
     */
    void restoreState(Object object);

}
