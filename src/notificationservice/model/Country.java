package notificationservice.model;

/**
 * Countries we can deliver to. A recipient's country is worked out from the dial code of its
 * international (+&lt;code&gt;&lt;number&gt;) phone number.
 */
public enum Country {
    IN("91"),
    US("1"),
    GB("44"),
    AE("971"),
    SG("65");

    private final String dialCode;

    Country(String dialCode) {
        this.dialCode = dialCode;
    }

    public String getDialCode() {
        return dialCode;
    }

    public static Country fromPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.startsWith("+")) {
            throw new IllegalArgumentException(
                    "Recipient must be in international format (+<country code><number>): " + phoneNumber);
        }
        // Longest match wins, so a 3-digit code is never mistaken for a shorter one it starts with.
        Country match = null;
        for (Country country : values()) {
            if (phoneNumber.startsWith(country.dialCode, 1)
                    && (match == null || country.dialCode.length() > match.dialCode.length())) {
                match = country;
            }
        }
        if (match == null) {
            throw new IllegalArgumentException("Unsupported country for recipient " + phoneNumber);
        }
        return match;
    }
}
