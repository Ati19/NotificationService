package notificationservice.strategy;

import notificationservice.model.Channel;
import notificationservice.model.ChannelConfig;
import notificationservice.model.Country;
import notificationservice.model.Notification;
import notificationservice.model.PartnerShare;
import notificationservice.provider.SmsProvider;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmsChannel implements NotificationChannel {
    private final Map<Country, Map<String, SmsProvider>> providersByCountry = new EnumMap<>(Country.class);
    private final PartnerSelector partnerSelector;

    /**
     * @param providersByCountry the partners operating in each country; a gateway that covers several
     *                           countries is listed under each of them
     */
    public SmsChannel(Map<Country, List<SmsProvider>> providersByCountry, PartnerSelector partnerSelector) {
        for (Map.Entry<Country, List<SmsProvider>> e : providersByCountry.entrySet()) {
            Map<String, SmsProvider> byName = new HashMap<>();
            for (SmsProvider provider : e.getValue()) {
                byName.put(provider.getName(), provider);
            }
            this.providersByCountry.put(e.getKey(), byName);
        }
        this.partnerSelector = partnerSelector;
    }

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }

    @Override
    public Set<String> getPartnerNames(Country country) {
        return Collections.unmodifiableSet(
                providersByCountry.getOrDefault(country, Collections.emptyMap()).keySet());
    }

    @Override
    public String send(Notification notification, ChannelConfig tenantConfig) {
        Country country = notification.getCountry();
        List<PartnerShare> shares = tenantConfig.getPartnerShares(country);
        if (shares == null) {
            throw new IllegalStateException("SMS to " + country + " not configured");
        }
        String partner = partnerSelector.select(
                notification.getTenantId() + ":" + Channel.SMS + ":" + country, shares);
        SmsProvider provider = providersByCountry.getOrDefault(country, Collections.emptyMap()).get(partner);
        if (provider == null) {
            throw new IllegalStateException("Unknown SMS partner in " + country + ": " + partner);
        }
        provider.sendSms(tenantConfig.getSenderId(), notification.getRecipient(), notification.getMessage());
        return partner;
    }
}
