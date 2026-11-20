package notificationservice.strategy;

import notificationservice.model.Channel;
import notificationservice.model.ChannelConfig;
import notificationservice.model.Country;
import notificationservice.model.Notification;

import java.util.Set;

/**
 * One implementation per channel (SMS, IVR, ...). Adding a channel = new class + register in the factory.
 */
public interface NotificationChannel {
    Channel getChannel();

    /**
     * @return names of the partners this channel can route through in the country (empty if none)
     */
    Set<String> getPartnerNames(Country country);

    /**
     * @return name of the partner the notification was sent through
     */
    String send(Notification notification, ChannelConfig tenantConfig);
}
