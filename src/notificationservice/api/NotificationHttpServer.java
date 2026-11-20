package notificationservice.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import notificationservice.model.Channel;
import notificationservice.model.ChannelConfig;
import notificationservice.model.Country;
import notificationservice.model.Notification;
import notificationservice.model.NotificationRequest;
import notificationservice.model.NotificationStatus;
import notificationservice.model.PartnerShare;
import notificationservice.service.NotFoundException;
import notificationservice.service.NotificationService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP front for {@link NotificationService}, built on the JDK's HttpServer (no external dependencies).
 *
 * <pre>
 * POST /tenants/{tenantId}/sms                                                        send an SMS from a template
 * GET  /tenants/{tenantId}/channels/{channel}/partner-shares                          partner split for every country
 * GET  /tenants/{tenantId}/channels/{channel}/countries/{country}/partner-shares      partner split for one country
 * PUT  /tenants/{tenantId}/channels/{channel}/countries/{country}/partner-shares      replace (or add) one country's split
 * GET  /tenants/{tenantId}/notifications                                              notifications sent for the tenant
 * </pre>
 */
public class NotificationHttpServer {
    private static final Pattern SMS_PATH = Pattern.compile("^/tenants/([^/]+)/sms/?$");
    private static final Pattern SHARES_PATH = Pattern.compile("^/tenants/([^/]+)/channels/([^/]+)/partner-shares/?$");
    private static final Pattern COUNTRY_SHARES_PATH =
            Pattern.compile("^/tenants/([^/]+)/channels/([^/]+)/countries/([^/]+)/partner-shares/?$");
    private static final Pattern NOTIFICATIONS_PATH = Pattern.compile("^/tenants/([^/]+)/notifications/?$");

    private final NotificationService service;
    private final HttpServer server;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    public NotificationHttpServer(NotificationService service, int port) throws IOException {
        this.service = service;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/tenants/", this::handle);
        server.setExecutor(executor);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
        executor.shutdown();
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            Matcher m;
            if ((m = SMS_PATH.matcher(path)).matches()) {
                requireMethod(method, "POST");
                sendSms(exchange, m.group(1));
            } else if ((m = SHARES_PATH.matcher(path)).matches()) {
                requireMethod(method, "GET");
                respond(exchange, 200, toJson(service.getChannelConfig(m.group(1), parseChannel(m.group(2)))));
            } else if ((m = COUNTRY_SHARES_PATH.matcher(path)).matches()) {
                Channel channel = parseChannel(m.group(2));
                Country country = parseCountry(m.group(3));
                if ("GET".equals(method)) {
                    respond(exchange, 200, toJson(country, service.getPartnerShares(m.group(1), channel, country)));
                } else {
                    requireMethod(method, "PUT");
                    updatePartnerShares(exchange, m.group(1), channel, country);
                }
            } else if ((m = NOTIFICATIONS_PATH.matcher(path)).matches()) {
                requireMethod(method, "GET");
                List<Object> body = new ArrayList<>();
                for (Notification n : service.getNotifications(m.group(1))) {
                    body.add(toJson(n));
                }
                respond(exchange, 200, body);
            } else {
                respond(exchange, 404, error("No route for " + path));
            }
        } catch (MethodNotAllowedException e) {
            respond(exchange, 405, error(e.getMessage()));
        } catch (NotFoundException e) {
            respond(exchange, 404, error(e.getMessage()));
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            respond(exchange, 400, error(e.getMessage()));
        } catch (IllegalStateException e) {
            respond(exchange, 409, error(e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            respond(exchange, 500, error("Internal error"));
        }
    }

    /**
     * Body: {"to": "+91...", "templateId": "TPL_OTP", "params": {"otp": "1234", "validity": "5"}}
     */
    private void sendSms(HttpExchange exchange, String tenantId) throws IOException {
        Map<String, Object> body = readObject(exchange);
        String to = requireString(body, "to");
        String templateId = requireString(body, "templateId");
        Map<String, String> params = new LinkedHashMap<>();
        Object rawParams = body.get("params");
        if (rawParams != null) {
            if (!(rawParams instanceof Map)) {
                throw new IllegalArgumentException("'params' must be an object");
            }
            for (Map.Entry<?, ?> e : ((Map<?, ?>) rawParams).entrySet()) {
                params.put(String.valueOf(e.getKey()), e.getValue() == null ? null : String.valueOf(e.getValue()));
            }
        }

        Notification notification = service.send(new NotificationRequest(tenantId, Channel.SMS, to, templateId, params));
        // The request was valid but the partner failed to deliver it.
        int status = notification.getStatus() == NotificationStatus.FAILED ? 502 : 201;
        respond(exchange, status, toJson(notification));
    }

    /**
     * Body: {"shares": [{"partner": "AIRTEL", "percentage": 20}, {"partner": "JIO", "percentage": 80}]}
     */
    private void updatePartnerShares(HttpExchange exchange, String tenantId, Channel channel, Country country)
            throws IOException {
        Map<String, Object> body = readObject(exchange);
        Object rawShares = body.get("shares");
        if (!(rawShares instanceof List)) {
            throw new IllegalArgumentException("'shares' must be an array");
        }
        List<PartnerShare> shares = new ArrayList<>();
        for (Object item : (List<?>) rawShares) {
            if (!(item instanceof Map)) {
                throw new IllegalArgumentException("Each share must be an object");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> share = (Map<String, Object>) item;
            Object percentage = share.get("percentage");
            if (!(percentage instanceof Long)) {
                throw new IllegalArgumentException("'percentage' must be a whole number");
            }
            shares.add(new PartnerShare(requireString(share, "partner"), Math.toIntExact((Long) percentage)));
        }
        respond(exchange, 200, toJson(country, service.updatePartnerShares(tenantId, channel, country, shares)));
    }

    private static Channel parseChannel(String raw) {
        try {
            return Channel.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Unknown channel: " + raw);
        }
    }

    private static Country parseCountry(String raw) {
        try {
            return Country.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Unknown country: " + raw);
        }
    }

    private static void requireMethod(String actual, String expected) {
        if (!expected.equals(actual)) {
            throw new MethodNotAllowedException(actual + " not allowed, use " + expected);
        }
    }

    private static String requireString(Map<String, Object> body, String field) {
        Object value = body.get(field);
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new IllegalArgumentException("'" + field + "' is required and must be a string");
        }
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readObject(HttpExchange exchange) throws IOException {
        String text;
        try (InputStream in = exchange.getRequestBody()) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException("Request body is required");
        }
        Object parsed = Json.parse(text);
        if (!(parsed instanceof Map)) {
            throw new IllegalArgumentException("Request body must be a JSON object");
        }
        return (Map<String, Object>) parsed;
    }

    private static void respond(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = Json.write(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        return body;
    }

    private static Map<String, Object> toJson(Notification n) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", n.getId());
        body.put("tenantId", n.getTenantId());
        body.put("channel", n.getChannel().name());
        body.put("to", n.getRecipient());
        body.put("country", n.getCountry().name());
        body.put("templateId", n.getTemplateId());
        body.put("message", n.getMessage());
        body.put("partner", n.getPartner());
        body.put("status", n.getStatus().name());
        body.put("failureReason", n.getFailureReason());
        body.put("createdAt", n.getCreatedAt().toString());
        return body;
    }

    /**
     * {"senderId": "ACME", "countries": {"IN": [{"partner": "AIRTEL", "percentage": 70}, ...], "US": [...]}}
     */
    private static Map<String, Object> toJson(ChannelConfig config) {
        Map<String, Object> countries = new LinkedHashMap<>();
        for (Map.Entry<Country, List<PartnerShare>> e : config.getSharesByCountry().entrySet()) {
            countries.put(e.getKey().name(), sharesToJson(e.getValue()));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("senderId", config.getSenderId());
        body.put("countries", countries);
        return body;
    }

    /**
     * {"country": "IN", "shares": [{"partner": "AIRTEL", "percentage": 70}, ...]}
     */
    private static Map<String, Object> toJson(Country country, List<PartnerShare> partnerShares) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("country", country.name());
        body.put("shares", sharesToJson(partnerShares));
        return body;
    }

    private static List<Object> sharesToJson(List<PartnerShare> partnerShares) {
        List<Object> shares = new ArrayList<>();
        for (PartnerShare share : partnerShares) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("partner", share.getPartnerName());
            s.put("percentage", share.getPercentage());
            shares.add(s);
        }
        return shares;
    }

    private static class MethodNotAllowedException extends RuntimeException {
        MethodNotAllowedException(String message) {
            super(message);
        }
    }
}
