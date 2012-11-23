package put.medicallocator.ui.misc;

import java.lang.ref.WeakReference;

import put.medicallocator.R;
import put.medicallocator.io.model.FacilityType;
import put.medicallocator.ui.ActivityMain;
import put.medicallocator.ui.async.SearchCriteria;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

public class LandscapeFacilityTypeInflateStrategy implements FacilityTypeInflateStrategy {

    private final WeakReference<Activity> context;
    private OnCheckedChangeListener listener;
    private final GridView facilityTypesGridView;
    
    public LandscapeFacilityTypeInflateStrategy(ActivityMain context, int gridViewId) {
        this.context = new WeakReference<Activity>(context);
        this.facilityTypesGridView = (GridView) context.findViewById(gridViewId);
    }
    
    @Override
    public void inflate(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void updateState(SearchCriteria criteria) {
        this.facilityTypesGridView.setAdapter(new FacilityTypeAdapter(context.get(), android.R.id.text1, FacilityType.values(), listener, criteria));
    }

    private class FacilityTypeAdapter extends ArrayAdapter<FacilityType> {

        private final OnCheckedChangeListener listener;
        private final SearchCriteria criteria;
        
        public FacilityTypeAdapter(Context context, int textViewResourceId, FacilityType[] objects, 
                OnCheckedChangeListener listener, SearchCriteria criteria) {
            super(context, textViewResourceId, objects);
            this.listener = listener;
            this.criteria = criteria;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final FacilityType type = getItem(position);

            if (convertView == null) {
                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.facilitytype_item, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.filter_checkbox);
                viewHolder.imageButton = (ImageButton) convertView.findViewById(R.id.filter_image);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.filter_textview);
                convertView.setTag(viewHolder);
            }
            
            final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            final CheckBox checkBox = viewHolder.checkBox;
            checkBox.setTag(type);
            checkBox.setOnCheckedChangeListener(listener);
            checkBox.setChecked(criteria.getAllowedTypes().contains(type));

            final ImageButton button = viewHolder.imageButton;
            button.setImageDrawable(getContext().getResources().getDrawable(type.getDrawableId()));
            
            final TextView textView = viewHolder.textView;
            textView.setText(type.getStringId());

            return convertView;
        }
        
    }
    
    private static final class ViewHolder {
        CheckBox checkBox;
        ImageButton imageButton;
        TextView textView;
    }
    
}
