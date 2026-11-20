package notificationservice.service;

/**
 * A tenant or template that doesn't exist (or isn't visible to the caller). Lets the HTTP layer answer 404.
 */
public class NotFoundException extends IllegalArgumentException {
    public NotFoundException(String message) {
        super(message);
    }
}
