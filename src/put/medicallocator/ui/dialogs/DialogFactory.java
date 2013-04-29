package put.medicallocator.ui.dialogs;

import android.app.Dialog;
import android.content.Context;

/**
 * Simple factory for the any kind of {@link Dialog} instances.
 */
public interface DialogFactory {

    /**
     * Creates the {@link Dialog} instance.
     */
    Dialog createDialog(Context context);
}
