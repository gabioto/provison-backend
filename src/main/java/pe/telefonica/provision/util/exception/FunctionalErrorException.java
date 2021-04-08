package pe.telefonica.provision.util.exception;

public class FunctionalErrorException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private String errorCode;
	

    public FunctionalErrorException(String message) {
        super(message);
    }

    public FunctionalErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public FunctionalErrorException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
