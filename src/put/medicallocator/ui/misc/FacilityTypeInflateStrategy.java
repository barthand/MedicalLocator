package put.medicallocator.ui.misc;

import put.medicallocator.ui.async.SearchCriteria;
import android.widget.CompoundButton.OnCheckedChangeListener;

public interface FacilityTypeInflateStrategy {

    void inflate(OnCheckedChangeListener listener);
    void updateState(SearchCriteria criteria);
    
}
