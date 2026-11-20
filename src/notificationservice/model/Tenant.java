package notificationservice.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Tenant {
    private final String id;
    private final String name;
    private boolean active = true;
    // Concurrent because partner shares can be changed while messages are being sent.
    private final Map<Channel, ChannelConfig> channelConfigs = new ConcurrentHashMap<>();

    public Tenant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void enableChannel(Channel channel, ChannelConfig config) {
        channelConfigs.put(channel, config);
    }

    public ChannelConfig getConfig(Channel channel) {
        return channelConfigs.get(channel);
    }

    public boolean isChannelEnabled(Channel channel) {
        return channelConfigs.containsKey(channel);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
