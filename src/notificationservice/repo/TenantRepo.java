package notificationservice.repo;

import notificationservice.model.Tenant;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TenantRepo {
    private final Map<String, Tenant> tenants = new ConcurrentHashMap<>();

    public void save(Tenant tenant) {
        tenants.put(tenant.getId(), tenant);
    }

    public Optional<Tenant> findById(String tenantId) {
        return Optional.ofNullable(tenants.get(tenantId));
    }
}
