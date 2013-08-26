package put.medicallocator.io.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import put.medicallocator.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing the types available in {@link Facility} objects.
 */
public enum FacilityType {

    CENTRE(0, R.drawable.ic_map_centre, R.string.facilitytype_centre),
    DENTIST(1, R.drawable.ic_map_dentist, R.string.facilitytype_dentist),
    EYE_DOCTOR(2, R.drawable.ic_map_eyedoctor, R.string.facilitytype_eye_doctor),
    GYNECOLOGIST(3, R.drawable.ic_map_gynecologist, R.string.facilitytype_gynecologist),
    DOCTOR(4, R.drawable.ic_map_doctor, R.string.facilitytype_doctor),
    HOSPITAL(5, R.drawable.ic_map_hospital, R.string.facilitytype_hospital),
    AMBULATORY(6, R.drawable.ic_map_centre, R.string.facilitytype_ambulatory),
    OTHER(100, R.drawable.ic_map_centre, R.string.facilitytype_other);

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

    /**
     * Returns ID that is associated with particular {@link FacilityType} in the dataset.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns ID of the {@link Drawable} (from resources) that is associated with this {@link FacilityType}.
     */
    public int getDrawableId() {
        return drawableId;
    }

    /**
     * Returns ID of the String resource that is associated with this {@link FacilityType}.
     */
    public int getStringId() {
        return stringId;
    }

    /**
     * Returns {@link FacilityType} associated with particular ID.
     */
    public static FacilityType getById(Integer id) {
        return FACILITY_TYPE_MAP.get(id);
    }

    /**
     * Returns the string label associated with this {@link FacilityType}.
     */
    public String getLabel(Context context) {
        return context.getString(this.getStringId());
    }

}
