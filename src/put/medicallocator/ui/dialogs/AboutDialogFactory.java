package put.medicallocator.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import put.medicallocator.R;

/**
 * {@link DialogFactory} for the 'about' dialog.
 */
public class AboutDialogFactory implements DialogFactory {

    @Override
    public Dialog createDialog(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_about, null);

        String version = null;
        try {
            final String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            version = String.format(context.getString(R.string.app_version), versionName);
        } catch (PackageManager.NameNotFoundException e) {
            // No version found, not critical..
        }

        final TextView versionTextView = (TextView) layout.findViewById(R.id.dialogabout_appversion);
        versionTextView.setText(version);

        final AlertDialog dialog = new AlertDialog.Builder(context).setView(layout).create();
        dialog.setTitle(context.getString(R.string.app_name));
        return dialog;
    }

}
