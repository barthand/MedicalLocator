package put.medicallocator.io.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import put.medicallocator.R;

public enum FacilityType {

    CENTRE(0, R.drawable.marker, R.string.facilitytype_centre),
    DENTIST(1, R.drawable.marker_blue, R.string.facilitytype_dentist),
    EYE_DOCTOR(2, R.drawable.marker_cyan, R.string.facilitytype_eye_doctor),
    GYNECOLOGIST(3, R.drawable.marker_pink, R.string.facilitytype_gynecologist),
    DOCTOR(4, R.drawable.marker_red, R.string.facilitytype_doctor),
    HOSPITAL(5, R.drawable.marker_yellow, R.string.facilitytype_hospital),
    AMBULATORY(6, R.drawable.marker, R.string.facilitytype_ambulatory),
    OTHER(100, R.drawable.marker, R.string.facilitytype_other);

    private static final Map<Integer, FacilityType> FACILITY_TYPE_MAP; 
    static {
        Map<Integer, FacilityType> map = new HashMap<Integer, FacilityType>();
        for (FacilityType type : FacilityType.values()) {
            map.put(type.getId(), type);
            
        }
        FACILITY_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private final int id;
    
    private final int drawableId;
    
    private final int stringId;

    private FacilityType(int id, int drawableId, int stringId) {
        this.id = id;
        this.drawableId = drawableId;
        this.stringId = stringId;
    }

    public int getId() {
        return id;
    }

    public int getDrawableId() {
        return drawableId;
    }
    
    public int getStringId() {
        return stringId;
    }

    public static FacilityType getById(Integer id) {
        return FACILITY_TYPE_MAP.get(id);
    }
    
}
