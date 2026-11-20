package notificationservice;

import notificationservice.factory.ChannelFactory;
import notificationservice.model.Channel;
import notificationservice.model.ChannelConfig;
import notificationservice.model.Country;
import notificationservice.model.Notification;
import notificationservice.model.NotificationRequest;
import notificationservice.model.PartnerShare;
import notificationservice.model.Template;
import notificationservice.model.Tenant;
import notificationservice.provider.ConsoleSmsProvider;
import notificationservice.provider.SmsProvider;
import notificationservice.repo.NotificationRepo;
import notificationservice.repo.TemplateRepo;
import notificationservice.repo.TenantRepo;
import notificationservice.service.NotificationService;
import notificationservice.service.TemplateService;
import notificationservice.strategy.PartnerSelector;
import notificationservice.strategy.SmsChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    private static final String IN_PREFIX = "+91990000000";
    private static final String US_PREFIX = "+1415555000";
    private static final String GB_PREFIX = "+44770090000";

    public static void main(String[] args) {
        // Which partners operate in which country. SINCH is one gateway that covers all three.
        SmsProvider sinch = new ConsoleSmsProvider("SINCH");
        Map<Country, List<SmsProvider>> smsPartners = new EnumMap<>(Country.class);
        smsPartners.put(Country.IN, Arrays.asList(new ConsoleSmsProvider("AIRTEL"), new ConsoleSmsProvider("JIO"), sinch));
        smsPartners.put(Country.US, Arrays.asList(new ConsoleSmsProvider("TWILIO"), sinch));
        smsPartners.put(Country.GB, Arrays.asList(new ConsoleSmsProvider("VODAFONE"), sinch));

        ChannelFactory channelFactory = new ChannelFactory();
        channelFactory.register(new SmsChannel(smsPartners, new PartnerSelector()));
        // Future: channelFactory.register(new IvrChannel(...));

        TenantRepo tenantRepo = new TenantRepo();
        Map<Country, List<PartnerShare>> acmeShares = new EnumMap<>(Country.class);
        acmeShares.put(Country.IN, Arrays.asList(new PartnerShare("AIRTEL", 70), new PartnerShare("JIO", 30)));
        acmeShares.put(Country.US, Collections.singletonList(new PartnerShare("TWILIO", 100)));
        Tenant acme = new Tenant("t1", "Acme");
        acme.enableChannel(Channel.SMS, new ChannelConfig("ACME", acmeShares));

        Map<Country, List<PartnerShare>> globexShares = new EnumMap<>(Country.class);
        globexShares.put(Country.IN, Collections.singletonList(new PartnerShare("JIO", 100)));
        globexShares.put(Country.US, Arrays.asList(new PartnerShare("TWILIO", 50), new PartnerShare("SINCH", 50)));
        Tenant globex = new Tenant("t2", "Globex");
        globex.enableChannel(Channel.SMS, new ChannelConfig("GLOBEX", globexShares));
        tenantRepo.save(acme);
        tenantRepo.save(globex);

        TemplateService templateService = new TemplateService(new TemplateRepo());
        NotificationService service = new NotificationService(
                tenantRepo, new NotificationRepo(), channelFactory, templateService);

        System.out.println("== Templates available to Acme: " + templateService.listTemplates("t1"));
        System.out.println("== Acme SMS config: " + service.getChannelConfig("t1", Channel.SMS));

        // API 1: send SMS, routed by the recipient's country
        System.out.println("\n== Acme: 10 OTPs to India (70% Airtel / 30% Jio)");
        Map<String, Integer> acmeIndia = sendOtps(service, "t1", IN_PREFIX, 10);
        System.out.println("Acme India per-partner count: " + acmeIndia);
        check(acmeIndia.getOrDefault("AIRTEL", 0) == 7 && acmeIndia.getOrDefault("JIO", 0) == 3,
                "India follows 70/30");

        System.out.println("\n== Acme: 10 OTPs to the US (100% Twilio)");
        Map<String, Integer> acmeUs = sendOtps(service, "t1", US_PREFIX, 10);
        System.out.println("Acme US per-partner count: " + acmeUs);
        check(acmeUs.getOrDefault("TWILIO", 0) == 10, "US goes only to Twilio");

        System.out.println("\n== Globex: 10 OTPs to the US (50% Twilio / 50% Sinch)");
        Map<String, Integer> globexUs = sendOtps(service, "t2", US_PREFIX, 10);
        System.out.println("Globex US per-partner count: " + globexUs);
        check(globexUs.getOrDefault("TWILIO", 0) == 5 && globexUs.getOrDefault("SINCH", 0) == 5,
                "Globex has its own US split");

        // API 2: change one country's partner share at runtime
        System.out.println("\n== Acme switches India to 20% Airtel / 80% Jio");
        System.out.println("Updated: " + service.updatePartnerShares("t1", Channel.SMS, Country.IN,
                Arrays.asList(new PartnerShare("AIRTEL", 20), new PartnerShare("JIO", 80))));
        System.out.println("Acme SMS config: " + service.getChannelConfig("t1", Channel.SMS));
        Map<String, Integer> afterUpdate = sendOtps(service, "t1", IN_PREFIX, 10);
        System.out.println("Acme India per-partner count after update: " + afterUpdate);
        check(afterUpdate.getOrDefault("AIRTEL", 0) == 2 && afterUpdate.getOrDefault("JIO", 0) == 8,
                "new India split 20/80 applied");
        Map<String, Integer> usAfterUpdate = sendOtps(service, "t1", US_PREFIX, 10);
        check(usAfterUpdate.getOrDefault("TWILIO", 0) == 10, "US split untouched by the India update");

        System.out.println("\n== Acme starts sending to the UK");
        tryToSend(service, otp("t1", GB_PREFIX + "1", 1234));    // GB not set up yet
        System.out.println("Added: " + service.updatePartnerShares("t1", Channel.SMS, Country.GB,
                Arrays.asList(new PartnerShare("VODAFONE", 60), new PartnerShare("SINCH", 40))));
        Map<String, Integer> acmeGb = sendOtps(service, "t1", GB_PREFIX, 10);
        System.out.println("Acme UK per-partner count: " + acmeGb);
        check(acmeGb.getOrDefault("VODAFONE", 0) == 6 && acmeGb.getOrDefault("SINCH", 0) == 4,
                "UK follows 60/40");

        System.out.println("\n== Partner share updates that must be rejected");
        tryToUpdate(service, "t1", Channel.SMS, Country.IN,
                Arrays.asList(new PartnerShare("AIRTEL", 50), new PartnerShare("JIO", 30)));      // sums to 80
        tryToUpdate(service, "t1", Channel.SMS, Country.IN,
                Arrays.asList(new PartnerShare("AIRTEL", 50), new PartnerShare("TWILIO", 50)));   // Twilio isn't in India
        tryToUpdate(service, "t1", Channel.SMS, Country.SG,
                Collections.singletonList(new PartnerShare("SINCH", 100)));                       // no partners in SG
        tryToUpdate(service, "t1", Channel.IVR, Country.IN,
                Collections.singletonList(new PartnerShare("AIRTEL", 100)));                      // IVR not enabled
        tryToUpdate(service, "nope", Channel.SMS, Country.IN,
                Collections.singletonList(new PartnerShare("AIRTEL", 100)));                      // unknown tenant
        Map<String, Integer> afterRejected = sendOtps(service, "t1", IN_PREFIX, 10);
        System.out.println("Acme India per-partner count after rejected updates: " + afterRejected);
        check(afterRejected.getOrDefault("AIRTEL", 0) == 2 && afterRejected.getOrDefault("JIO", 0) == 8,
                "rejected updates left the 20/80 split in place");

        System.out.println("\n== Globex creates its own template");
        Template orderTemplate = templateService.createTemplate("t2", "Order shipped",
                "Hi {{name}}, your order {{orderId}} has been shipped.");
        System.out.println("Created: " + orderTemplate);
        Map<String, String> orderParams = new HashMap<>();
        orderParams.put("name", "Ravi");
        orderParams.put("orderId", "ORD-42");
        System.out.println(service.send(new NotificationRequest("t2", Channel.SMS, "+912222222222",
                orderTemplate.getId(), orderParams)));

        System.out.println("\n== Validation");
        tryToSend(service, new NotificationRequest("t1", Channel.SMS, "+911111111111",
                orderTemplate.getId(), orderParams));            // Globex's template, not Acme's
        tryToSend(service, new NotificationRequest("t1", Channel.SMS, "+911111111111",
                TemplateService.OTP_TEMPLATE_ID, Collections.singletonMap("otp", "9999"))); // missing validity
        tryToSend(service, new NotificationRequest("t1", Channel.IVR, "+911111111111",
                TemplateService.OTP_TEMPLATE_ID, null));         // IVR not enabled
        tryToSend(service, otp("t2", GB_PREFIX + "1", 1234));    // Globex hasn't set up the UK
        tryToSend(service, otp("t1", "+8613800000000", 1234));   // country we don't deliver to
        tryToSend(service, otp("t1", "9900000000", 1234));       // no country code
        try {
            new ChannelConfig("BAD", Collections.singletonMap(Country.IN,
                    Arrays.asList(new PartnerShare("AIRTEL", 60), new PartnerShare("JIO", 30))));
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected config: " + e.getMessage());
        }
    }

    /**
     * Sends {@code count} OTPs for the tenant to numbers starting with {@code recipientPrefix} and
     * returns how many went through each partner in this batch.
     */
    private static Map<String, Integer> sendOtps(NotificationService service, String tenantId,
                                                 String recipientPrefix, int count) {
        Map<String, Integer> perPartner = new TreeMap<>();
        for (int i = 0; i < count; i++) {
            Notification n = service.send(otp(tenantId, recipientPrefix + i, 1000 + i));
            perPartner.merge(n.getPartner(), 1, Integer::sum);
        }
        return perPartner;
    }

    private static NotificationRequest otp(String tenantId, String recipient, int otp) {
        Map<String, String> params = new HashMap<>();
        params.put("otp", String.valueOf(otp));
        params.put("validity", "5");
        return new NotificationRequest(tenantId, Channel.SMS, recipient, TemplateService.OTP_TEMPLATE_ID, params);
    }

    private static void tryToUpdate(NotificationService service, String tenantId, Channel channel,
                                    Country country, List<PartnerShare> shares) {
        try {
            service.updatePartnerShares(tenantId, channel, country, shares);
            System.out.println("UNEXPECTED: update accepted");
        } catch (RuntimeException e) {
            System.out.println("Rejected update: " + e.getMessage());
        }
    }

    private static void check(boolean condition, String what) {
        System.out.println((condition ? "PASS: " : "FAIL: ") + what);
    }

    private static void tryToSend(NotificationService service, NotificationRequest request) {
        try {
            service.send(request);
            System.out.println("UNEXPECTED: send accepted");
        } catch (RuntimeException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}
