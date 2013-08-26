package put.medicallocator.ui.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import put.medicallocator.R;

/**
 * {@link DialogFactory} for the dialog informing user that data source is being initialized.
 */
public class DataSourceConfigurationDialogFactory implements DialogFactory {

    @Override
    public Dialog createDialog(Context context) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.activitymain_initializing_provider));
        return dialog;
    }
}
