package put.medicallocator.io;

import java.util.List;


import com.google.android.maps.GeoPoint;

public interface IFacilityDAO {

	List<Facility> findWithinArea(GeoPoint lowerLeft, GeoPoint upperRight) throws DAOException;

	List<Facility> findNamedWithinArea(GeoPoint lowerLeft, GeoPoint upperRight, String[] names) throws DAOException;

	List<Facility> findWithAddress(String address) throws DAOException;

	List<Facility> findWithKeyword(String keyword) throws DAOException;

}