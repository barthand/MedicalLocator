package put.medicallocator.ui.async;

import java.util.HashSet;
import java.util.Set;

import put.medicallocator.io.model.FacilityType;

public class SearchCriteria {
    
    private String query;    
    private Set<FacilityType> allowedTypes;
    private long lastChangeTimestamp = System.currentTimeMillis();

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
        if (allowedTypes == null) {
            allowedTypes = new HashSet<FacilityType>();
        }
        allowedTypes.add(facilityType);
    }
    
    public boolean removeAllowedType(FacilityType facilityType) {
        lastChangeTimestamp = System.currentTimeMillis();
        if (allowedTypes == null) {
            return false;
        }
        return allowedTypes.remove(facilityType);
    }

    public void setAllowedTypes(Set<FacilityType> allowedTypes) {
        lastChangeTimestamp = System.currentTimeMillis();
        this.allowedTypes = allowedTypes;
    }

    public long getLastChangeTimestamp() {
        return lastChangeTimestamp;
    }
    
}
