package notificationservice.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message body with {{placeholders}}, e.g. "Your OTP is {{otp}}".
 * tenantId == null means a built-in template every tenant can use.
 */
public class Template {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");

    private final String id;
    private final String tenantId;
    private final String name;
    private final String content;
    private final Set<String> placeholders;

    public Template(String id, String tenantId, String name, String content) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.content = content;
        Set<String> found = new LinkedHashSet<>();
        Matcher m = PLACEHOLDER.matcher(content);
        while (m.find()) {
            found.add(m.group(1));
        }
        this.placeholders = Collections.unmodifiableSet(found);
    }

    public String render(Map<String, String> params) {
        for (String key : placeholders) {
            if (params == null || !params.containsKey(key)) {
                throw new IllegalArgumentException("Missing value for placeholder '" + key + "' in template " + id);
            }
        }
        Matcher m = PLACEHOLDER.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(params.get(m.group(1))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public boolean isAccessibleBy(String tenantId) {
        return this.tenantId == null || this.tenantId.equals(tenantId);
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public Set<String> getPlaceholders() {
        return placeholders;
    }

    @Override
    public String toString() {
        return "Template{id=" + id + ", name=" + name + ", owner=" + (tenantId == null ? "SYSTEM" : tenantId)
                + ", placeholders=" + placeholders + "}";
    }
}
