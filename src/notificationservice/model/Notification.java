package notificationservice.model;

import java.time.Instant;
import java.util.UUID;

public class Notification {
    private final String id = UUID.randomUUID().toString();
    private final String tenantId;
    private final Channel channel;
    private final String recipient;
    private final Country country;
    private final String templateId;
    private final String message;
    private final Instant createdAt = Instant.now();
    private NotificationStatus status = NotificationStatus.PENDING;
    private String partner;
    private String failureReason;

    public Notification(NotificationRequest request, Country country, String renderedMessage) {
        this.tenantId = request.getTenantId();
        this.channel = request.getChannel();
        this.recipient = request.getRecipient();
        this.country = country;
        this.templateId = request.getTemplateId();
        this.message = renderedMessage;
    }

    public void markSent(String partner) {
        this.status = NotificationStatus.SENT;
        this.partner = partner;
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
    }

    public String getId() {
        return id;
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

    public Country getCountry() {
        return country;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getPartner() {
        return partner;
    }

    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String toString() {
        return "Notification{tenant=" + tenantId + ", channel=" + channel + ", to=" + recipient
                + ", country=" + country + ", template=" + templateId + ", partner=" + partner + ", status=" + status
                + (failureReason != null ? ", reason=" + failureReason : "") + "}";
    }
}
