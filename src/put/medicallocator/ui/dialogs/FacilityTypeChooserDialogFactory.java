package put.medicallocator.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseBooleanArray;
import put.medicallocator.io.model.FacilityType;
import put.medicallocator.ui.async.model.SearchCriteria;
import put.medicallocator.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DialogFactory} for the dialog allowing to select user throughout available {@link FacilityType}s.
 */
public class FacilityTypeChooserDialogFactory implements DialogFactory {

    /* TODO:
     * * Add custom view for items with the icon, animation?
     * * Allow selecting/deselecting all?
     */

    private final SearchCriteria criteria;
    private final OnFacilitiesTypesSelectedListener listener;

    public FacilityTypeChooserDialogFactory(SearchCriteria criteria, OnFacilitiesTypesSelectedListener listener) {
        this.criteria = criteria;
        this.listener = listener;
    }

    @Override
    public Dialog createDialog(final Context context) {
        final FacilityType[] types = FacilityType.values();
        final MultichoiceContextDescriber describer = new MultichoiceContextDescriber(context, types, criteria);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMultiChoiceItems(describer.labels, describer.checked, null);

        final OnFacilitiesTypesSelectedListener listener = this.listener;
        final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final AlertDialog alertDialog = (AlertDialog) dialog;

                final List<FacilityType> result = new ArrayList<FacilityType>();
                final List<String> selectedLabels = new ArrayList<String>();

                final SparseBooleanArray array = alertDialog.getListView().getCheckedItemPositions();
                for (int index : CollectionUtils.getIndexesFromSparseBooleanArray(array, true)) {
                    result.add(types[index]);
                    selectedLabels.add(describer.labels[index]);
                }

                listener.onTypesSelected(result, selectedLabels.toArray(new String[selectedLabels.size()]));
            }
        };
        builder.setPositiveButton(context.getString(android.R.string.ok), onClickListener);
        builder.setNegativeButton(context.getString(android.R.string.cancel), null);
        return builder.create();
    }


    private static class MultichoiceContextDescriber {
        private final Context context;
        private final FacilityType[] availableTypes;
        private final SearchCriteria criteria;

        private final String[] labels;
        private final boolean[] checked;

        private MultichoiceContextDescriber(Context context, FacilityType[] availableTypes, SearchCriteria criteria) {
            this.context = context;
            this.availableTypes = availableTypes;
            this.criteria = criteria;

            this.labels = buildLabels();
            this.checked = buildCheckedArray();
        }


        private String[] buildLabels() {
            final String[] result = new String[availableTypes.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = availableTypes[i].getLabel(context);
            }
            return result;
        }

        private boolean[] buildCheckedArray() {
            final boolean[] checked = new boolean[availableTypes.length];
            for (int i = 0; i < availableTypes.length; i++) {
                checked[i] = criteria.getAllowedTypes().contains(availableTypes[i]);
            }
            return checked;
        }

    }

    public interface OnFacilitiesTypesSelectedListener {
        void onTypesSelected(List<FacilityType> types, String[] labels);
    }
}
