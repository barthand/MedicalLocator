package put.medicallocator.ui.async.model;

import android.content.Context;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.model.FacilityType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple structure storing information about the criteria used to query for the {@link Facility}.
 */
public class SearchCriteria {

    /**
     * Simple query {@link String}, used by the database back-end to perform the SQL LIKE operation.
     */
    private String query;

    /**
     * {@link Set} of elements of {@link FacilityType} type. Indicates the types of facilities taken into the account
     * while running the query.
     */
    private final Set<FacilityType> allowedTypes = new HashSet<FacilityType>();

    /**
     * Stores the timestamp of the last change done to this {@link SearchCriteria}.
     */
    private long lastChangeTimestamp = System.currentTimeMillis();

    public SearchCriteria() {
        this.allowedTypes.addAll(Arrays.asList(FacilityType.values()));
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        lastChangeTimestamp = System.currentTimeMillis();
        this.query = query;
    }

    public Set<FacilityType> getAllowedTypes() {
        return allowedTypes;
    }
    
    public void addAllowedType(FacilityType facilityType) {
        lastChangeTimestamp = System.currentTimeMillis();
        allowedTypes.add(facilityType);
    }
    
    public void setChecked(FacilityType facilityType, boolean isChecked) {
        if (isChecked) {
            addAllowedType(facilityType);
        } else {
            removeAllowedType(facilityType);
        }
    }
    
    public void removeAll() {
        lastChangeTimestamp = System.currentTimeMillis();
        allowedTypes.clear();
    }

    public boolean isEverythingAllowed() {
        return allowedTypes.size() == FacilityType.values().length;
    }

    public boolean removeAllowedType(FacilityType facilityType) {
        lastChangeTimestamp = System.currentTimeMillis();
        return allowedTypes.remove(facilityType);
    }

    public void setAllowedTypes(Collection<FacilityType> allowedTypes) {
        lastChangeTimestamp = System.currentTimeMillis();
        this.allowedTypes.clear();
        this.allowedTypes.addAll(allowedTypes);
    }

    public String[] getAllowedTypesLabels(Context context) {
        final String[] labels = new String[allowedTypes.size()];
        int i=0;
        for (FacilityType type : allowedTypes) {
            labels[i++] = type.getLabel(context);
        }
        return labels;
    }

    public long getLastChangeTimestamp() {
        return lastChangeTimestamp;
    }
    
}
