package notificationservice.service;

import notificationservice.model.Template;
import notificationservice.repo.TemplateRepo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TemplateService {
    public static final String OTP_TEMPLATE_ID = "TPL_OTP";
    public static final String LOGIN_TEMPLATE_ID = "TPL_LOGIN";

    private final TemplateRepo templateRepo;

    public TemplateService(TemplateRepo templateRepo) {
        this.templateRepo = templateRepo;
        loadDefaultTemplates();
    }

    private void loadDefaultTemplates() {
        templateRepo.save(new Template(OTP_TEMPLATE_ID, null, "OTP",
                "{{otp}} is your OTP. It is valid for {{validity}} minutes. Do not share it with anyone."));
        templateRepo.save(new Template(LOGIN_TEMPLATE_ID, null, "Login alert",
                "New login to your {{app}} account from {{device}} at {{time}}."));
    }

    public Template createTemplate(String tenantId, String name, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Template content cannot be empty");
        }
        String id = "TPL_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Template template = new Template(id, tenantId, name, content);
        templateRepo.save(template);
        return template;
    }

    public Template getTemplate(String tenantId, String templateId) {
        Template template = templateRepo.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Unknown template: " + templateId));
        // Don't reveal that another tenant's template exists.
        if (!template.isAccessibleBy(tenantId)) {
            throw new NotFoundException("Unknown template: " + templateId);
        }
        return template;
    }

    public List<Template> listTemplates(String tenantId) {
        return templateRepo.findAccessibleBy(tenantId);
    }

    public String render(String tenantId, String templateId, Map<String, String> params) {
        return getTemplate(tenantId, templateId).render(params);
    }
}
