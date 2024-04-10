package cs.youtrade.cstradeapp.util;

public class TmPingException extends RuntimeException{

    public TmPingException() {
        super();
    }

    public TmPingException(String message) {
        super(message);
    }

    public TmPingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TmPingException(Throwable cause) {
        super(cause);
    }

    protected TmPingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
