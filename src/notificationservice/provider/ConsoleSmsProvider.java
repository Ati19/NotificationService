package notificationservice.provider;

/**
 * Stand-in for a real gateway: prints instead of calling an external API.
 */
public class ConsoleSmsProvider implements SmsProvider {
    private final String name;

    public ConsoleSmsProvider(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendSms(String senderId, String to, String message) {
        System.out.println("[" + name + "] SMS from " + senderId + " to " + to + ": " + message);
    }
}
