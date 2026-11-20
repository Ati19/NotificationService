package notificationservice;

import notificationservice.api.NotificationHttpServer;
import notificationservice.factory.ChannelFactory;
import notificationservice.model.Channel;
import notificationservice.model.ChannelConfig;
import notificationservice.model.Country;
import notificationservice.model.PartnerShare;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Starts the HTTP API with the same seed data as {@link Main}. Usage: ServerMain [port] (default 8080).
 */
public class ServerMain {
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8089;

        SmsProvider sinch = new ConsoleSmsProvider("SINCH");
        Map<Country, List<SmsProvider>> smsPartners = new EnumMap<>(Country.class);
        smsPartners.put(Country.IN, Arrays.asList(new ConsoleSmsProvider("AIRTEL"), new ConsoleSmsProvider("JIO"), sinch));
        smsPartners.put(Country.US, Arrays.asList(new ConsoleSmsProvider("TWILIO"), sinch));
        smsPartners.put(Country.GB, Arrays.asList(new ConsoleSmsProvider("VODAFONE"), sinch));

        ChannelFactory channelFactory = new ChannelFactory();
        channelFactory.register(new SmsChannel(smsPartners, new PartnerSelector()));

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

        NotificationService service = new NotificationService(
                tenantRepo, new NotificationRepo(), channelFactory, new TemplateService(new TemplateRepo()));

        NotificationHttpServer server = new NotificationHttpServer(service, port);
        server.start();
        System.out.println("Notification API listening on http://localhost:" + server.getPort());
    }
}
