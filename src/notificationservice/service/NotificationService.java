package notificationservice.service;

import notificationservice.factory.ChannelFactory;
import notificationservice.model.Channel;
import notificationservice.model.ChannelConfig;
import notificationservice.model.Country;
import notificationservice.model.Notification;
import notificationservice.model.NotificationRequest;
import notificationservice.model.PartnerShare;
import notificationservice.model.Tenant;
import notificationservice.repo.NotificationRepo;
import notificationservice.repo.TenantRepo;

import java.util.List;
import java.util.Set;

/**
 * Public API of the module.
 */
public class NotificationService {
    private final TenantRepo tenantRepo;
    private final NotificationRepo notificationRepo;
    private final ChannelFactory channelFactory;
    private final TemplateService templateService;

    public NotificationService(TenantRepo tenantRepo, NotificationRepo notificationRepo,
                               ChannelFactory channelFactory, TemplateService templateService) {
        this.tenantRepo = tenantRepo;
        this.notificationRepo = notificationRepo;
        this.channelFactory = channelFactory;
        this.templateService = templateService;
    }

    public Notification send(NotificationRequest request) {
        Tenant tenant = getTenant(request.getTenantId());
        if (!tenant.isActive()) {
            throw new IllegalStateException("Tenant is inactive: " + tenant.getId());
        }
        if (!tenant.isChannelEnabled(request.getChannel())) {
            throw new IllegalStateException(request.getChannel() + " not enabled for tenant " + tenant.getId());
        }
        Country country = Country.fromPhoneNumber(request.getRecipient());
        ChannelConfig config = tenant.getConfig(request.getChannel());
        if (config.getPartnerShares(country) == null) {
            throw new IllegalStateException(
                    request.getChannel() + " to " + country + " not enabled for tenant " + tenant.getId());
        }

        String message = templateService.render(tenant.getId(), request.getTemplateId(), request.getParams());
        Notification notification = new Notification(request, country, message);
        notificationRepo.save(notification);

        try {
            String partner = channelFactory.get(request.getChannel()).send(notification, config);
            notification.markSent(partner);
        } catch (RuntimeException e) {
            notification.markFailed(e.getMessage());
        }
        return notification;
    }

    /**
     * Replaces how a tenant's traffic on a channel to one country is split across partners, or starts
     * sending to that country if it wasn't set up yet. Only partners operating in that country are
     * accepted. Takes effect from the next message; other countries and the sender id are untouched.
     * The old split stays in place if the new one is invalid.
     */
    public List<PartnerShare> updatePartnerShares(String tenantId, Channel channel, Country country,
                                                  List<PartnerShare> partnerShares) {
        Tenant tenant = getTenant(tenantId);
        // Read-modify-write of the tenant's config: serialise so concurrent updates to two
        // different countries don't overwrite each other.
        synchronized (tenant) {
            ChannelConfig current = tenant.getConfig(channel);
            if (current == null) {
                throw new IllegalStateException(channel + " not enabled for tenant " + tenantId);
            }
            Set<String> knownPartners = channelFactory.get(channel).getPartnerNames(country);
            for (PartnerShare share : partnerShares) {
                if (!knownPartners.contains(share.getPartnerName())) {
                    throw new IllegalArgumentException(
                            "Unknown " + channel + " partner in " + country + ": " + share.getPartnerName());
                }
            }
            ChannelConfig updated = current.withPartnerShares(country, partnerShares);
            tenant.enableChannel(channel, updated);
            return updated.getPartnerShares(country);
        }
    }

    public ChannelConfig getChannelConfig(String tenantId, Channel channel) {
        ChannelConfig config = getTenant(tenantId).getConfig(channel);
        if (config == null) {
            throw new IllegalStateException(channel + " not enabled for tenant " + tenantId);
        }
        return config;
    }

    public List<PartnerShare> getPartnerShares(String tenantId, Channel channel, Country country) {
        List<PartnerShare> shares = getChannelConfig(tenantId, channel).getPartnerShares(country);
        if (shares == null) {
            throw new NotFoundException(channel + " to " + country + " not enabled for tenant " + tenantId);
        }
        return shares;
    }

    public List<Notification> getNotifications(String tenantId) {
        getTenant(tenantId);
        return notificationRepo.findByTenant(tenantId);
    }

    private Tenant getTenant(String tenantId) {
        return tenantRepo.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Unknown tenant: " + tenantId));
    }
}
