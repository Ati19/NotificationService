package notificationservice.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-tenant settings for one channel: the sender id and, for every country the tenant sends to,
 * how that country's traffic is split across partners. Immutable; updates produce a new config.
 */
public class ChannelConfig {
    private final String senderId;
    private final Map<Country, List<PartnerShare>> sharesByCountry;

    public ChannelConfig(String senderId, Map<Country, List<PartnerShare>> sharesByCountry) {
        if (sharesByCountry == null || sharesByCountry.isEmpty()) {
            throw new IllegalArgumentException("At least one country is required");
        }
        Map<Country, List<PartnerShare>> copy = new EnumMap<>(Country.class);
        for (Map.Entry<Country, List<PartnerShare>> e : sharesByCountry.entrySet()) {
            copy.put(e.getKey(), validate(e.getKey(), e.getValue()));
        }
        this.senderId = senderId;
        this.sharesByCountry = Collections.unmodifiableMap(copy);
    }

    /**
     * A copy of this config with one country's split replaced, or added if the country is new.
     */
    public ChannelConfig withPartnerShares(Country country, List<PartnerShare> partnerShares) {
        Map<Country, List<PartnerShare>> updated = new EnumMap<>(sharesByCountry);
        updated.put(country, partnerShares);
        return new ChannelConfig(senderId, updated);
    }

    private static List<PartnerShare> validate(Country country, List<PartnerShare> partnerShares) {
        if (partnerShares == null || partnerShares.isEmpty()) {
            throw new IllegalArgumentException("At least one partner is required for " + country);
        }
        int total = partnerShares.stream().mapToInt(PartnerShare::getPercentage).sum();
        if (total != 100) {
            throw new IllegalArgumentException(
                    "Partner percentages for " + country + " must add up to 100, got " + total);
        }
        return Collections.unmodifiableList(new ArrayList<>(partnerShares));
    }

    public String getSenderId() {
        return senderId;
    }

    public Set<Country> getCountries() {
        return sharesByCountry.keySet();
    }

    /**
     * @return the split for the country, or null if the tenant hasn't set up this channel there
     */
    public List<PartnerShare> getPartnerShares(Country country) {
        return sharesByCountry.get(country);
    }

    public Map<Country, List<PartnerShare>> getSharesByCountry() {
        return sharesByCountry;
    }

    @Override
    public String toString() {
        return "ChannelConfig{senderId=" + senderId + ", shares=" + sharesByCountry + "}";
    }
}
