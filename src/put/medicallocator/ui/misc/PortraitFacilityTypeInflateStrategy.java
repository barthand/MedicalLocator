package put.medicallocator.ui.misc;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import put.medicallocator.R;
import put.medicallocator.io.model.FacilityType;
import put.medicallocator.ui.async.SearchCriteria;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class PortraitFacilityTypeInflateStrategy implements FacilityTypeInflateStrategy {

    private final WeakReference<Activity> context;
    private final int parentId;
    
    private final Map<FacilityType, FilterViewHolder> facilityTypeViewMapping;   
    
    public PortraitFacilityTypeInflateStrategy(Activity context, int parentId) {
        this.context = new WeakReference<Activity>(context);
        this.parentId = parentId;
        this.facilityTypeViewMapping = new HashMap<FacilityType, FilterViewHolder>();
    }
    
    @Override
    public void inflate(OnCheckedChangeListener listener) {
        final Activity context = this.context.get();
        final ViewGroup parent = (ViewGroup) context.findViewById(this.parentId);        
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        for (FacilityType type : FacilityType.values()) {
            final View item = inflater.inflate(R.layout.facilitytype_item, null);
            final CheckBox checkBox = (CheckBox) item.findViewById(R.id.filter_checkbox);
            checkBox.setTag(type);
            checkBox.setOnCheckedChangeListener(listener);            
            final ImageButton button = (ImageButton) item.findViewById(R.id.filter_image);
            final TextView textView = (TextView) item.findViewById(R.id.filter_textview);
            button.setImageDrawable(context.getResources().getDrawable(type.getDrawableId()));
            textView.setText(type.getStringId());
            parent.addView(item);
            this.facilityTypeViewMapping.put(type, new FilterViewHolder(type, checkBox));
        }
    }

    @Override
    public void updateState(SearchCriteria criteria) {
        for (Entry<FacilityType, FilterViewHolder> entry : facilityTypeViewMapping.entrySet()) {
            entry.getValue().checkBox.setChecked(criteria.getAllowedTypes().contains(entry.getKey()));
            Log.d("TAG", "Set " + entry.getKey().name() + " as checked[" + criteria.getAllowedTypes().contains(entry.getKey()) + "]");
        }        
    }

    private static class FilterViewHolder {
        private final FacilityType type;
        private final CheckBox checkBox;

        public FilterViewHolder(FacilityType type, CheckBox checkBox) {
            super();
            this.type = type;
            this.checkBox = checkBox;
        }
    }
    
}
