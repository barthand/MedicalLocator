package put.medicallocator.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import put.medicallocator.R;

public class CreditsDialogFactory implements DialogFactory {

    @Override
    public Dialog createDialog(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_credits, null);

        final TextView credits = (TextView) layout.findViewById(R.id.credits_textview);
        Linkify.addLinks(credits, Linkify.ALL);

        final AlertDialog dialog = new AlertDialog.Builder(context).setView(layout).create();
        dialog.setTitle(context.getString(R.string.credits));
        return dialog;
    }
}
