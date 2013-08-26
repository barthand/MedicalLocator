package put.medicallocator.io;

import com.google.android.maps.GeoPoint;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.model.FacilityType;
import put.medicallocator.ui.async.model.SearchCriteria;

import java.util.List;

/**
 * DAO interface describing contract used throughout this application.
 */
public interface IFacilityDAO {

    /**
     * Finds all {@link Facility} within the specified bounding box.
     */
    List<Facility> findWithinArea(GeoPoint lowerLeft, GeoPoint upperRight) throws DAOException;

    /**
     * Finds all {@link Facility} within the specified bounding box using the {@link SearchCriteria}.
     * Provided {@link SearchCriteria} are taken into account, in particular the query and allowed {@link FacilityType}s.
     */
    List<Facility> findWithinAreaUsingCriteria(GeoPoint lowerLeft, GeoPoint upperRight, SearchCriteria criteria) throws DAOException;

    /**
     * Finds all {@link Facility} which address matches the provided one (provided address may be a substring).
     */
    List<Facility> findWithAddress(String address) throws DAOException;

    /**
     * Finds all {@link Facility} which either address or name matches the provided keyword(s).
     */
    List<Facility> findWithKeyword(String keyword) throws DAOException;

    /**
     * Retrieves the {@link Facility} using its ID.
     */
    Facility findById(Long id) throws DAOException;

}