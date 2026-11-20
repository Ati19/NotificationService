package notificationservice.repo;

import notificationservice.model.Template;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TemplateRepo {
    private final Map<String, Template> templates = new ConcurrentHashMap<>();

    public void save(Template template) {
        templates.put(template.getId(), template);
    }

    public Optional<Template> findById(String templateId) {
        return Optional.ofNullable(templates.get(templateId));
    }

    /**
     * Built-in templates plus the tenant's own.
     */
    public List<Template> findAccessibleBy(String tenantId) {
        return templates.values().stream()
                .filter(t -> t.isAccessibleBy(tenantId))
                .collect(Collectors.toList());
    }
}
