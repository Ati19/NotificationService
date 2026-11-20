package notificationservice.provider;

/**
 * An SMS partner/operator gateway (Airtel, Jio, ...). Tenants refer to it by name.
 */
public interface SmsProvider {
    String getName();

    void sendSms(String senderId, String to, String message);
}
