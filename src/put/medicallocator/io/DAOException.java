package put.medicallocator.io;

/**
 * Generic {@link RuntimeException} associated with the {@link IFacilityDAO}.
 */
public class DAOException extends RuntimeException {

	private static final long serialVersionUID = 20120524160000L;

	public DAOException() {
		super();
	}

	public DAOException(String detailMessage) {
		super(detailMessage);
	}

    public DAOException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DAOException(Throwable throwable) {
        super(throwable);
    }

}
