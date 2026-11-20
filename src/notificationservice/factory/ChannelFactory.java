package notificationservice.factory;

import notificationservice.model.Channel;
import notificationservice.strategy.NotificationChannel;

import java.util.EnumMap;
import java.util.Map;

public class ChannelFactory {
    private final Map<Channel, NotificationChannel> channels = new EnumMap<>(Channel.class);

    public void register(NotificationChannel channel) {
        channels.put(channel.getChannel(), channel);
    }

    public NotificationChannel get(Channel channel) {
        NotificationChannel impl = channels.get(channel);
        if (impl == null) {
            throw new UnsupportedOperationException("Channel not supported yet: " + channel);
        }
        return impl;
    }
}
