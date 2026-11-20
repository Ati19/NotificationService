package notificationservice.repo;

import notificationservice.model.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notifications are partitioned by tenant so one tenant can never read another's data.
 */
public class NotificationRepo {
    private final Map<String, List<Notification>> byTenant = new ConcurrentHashMap<>();

    public void save(Notification notification) {
        byTenant.computeIfAbsent(notification.getTenantId(), k -> new CopyOnWriteArrayList<>()).add(notification);
    }

    public List<Notification> findByTenant(String tenantId) {
        return Collections.unmodifiableList(new ArrayList<>(byTenant.getOrDefault(tenantId, Collections.emptyList())));
    }
}
