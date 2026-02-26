package info_test;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;

@ApplicationScoped
public class NetworkInfoProvider implements InfoProvider {

    @Override
    public String name() {
        return "IP";
    }

    @Override
    public String get() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) {
            LOG.debug("No FacesContext (async/thread)");
            return "unknown";
        }

        Object reqObj = ctx.getExternalContext().getRequest();
        if (!(reqObj instanceof HttpServletRequest req)) {
            return "unknown";
        }

        String ip = fromHeaders(req);
        if (ip == null) {
            ip = normalize(req.getRemoteAddr());
        }

        LOG.debug("Resolved IP = {}", ip);
        return ip;
    }

    private String fromHeaders(HttpServletRequest req) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP"
        };

        for (String header : headers) {
            String value = req.getHeader(header);
            if (value == null || value.isBlank()) continue;

            for (String part : value.split(",")) {
                String ip = normalize(part.trim());
                if (ip != null) return ip;
            }
        }
        return null;
    }

    private String normalize(String ip) {
        try {
            return InetAddress.getByName(ip).getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }
}
