package put.medicallocator.io;

public class DAOException extends Exception {

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
