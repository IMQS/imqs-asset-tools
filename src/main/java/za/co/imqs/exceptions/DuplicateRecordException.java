package za.co.imqs.exceptions;

/**
 * Created by gerhardv on 2020-01-30.
 */
public class DuplicateRecordException extends RuntimeException {
    public DuplicateRecordException(String message) {
        super(message);
    }
}
