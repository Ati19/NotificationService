package notificationservice.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NotificationRequest {
    private final String tenantId;
    private final Channel channel;
    private final String recipient;
    private final String templateId;
    private final Map<String, String> params;

    public NotificationRequest(String tenantId, Channel channel, String recipient,
                               String templateId, Map<String, String> params) {
        this.tenantId = tenantId;
        this.channel = channel;
        this.recipient = recipient;
        this.templateId = templateId;
        this.params = params == null ? Collections.emptyMap() : new HashMap<>(params);
    }

    public String getTenantId() {
        return tenantId;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getTemplateId() {
        return templateId;
    }

    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }
}
