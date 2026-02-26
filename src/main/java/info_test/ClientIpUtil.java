
package info_test;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientIpUtil {

    private static final String[] IP_HEADERS = {
        "X-Forwarded-For",
        "X-Real-IP",
        "CF-Connecting-IP",
        "Forwarded"
    };

    private ClientIpUtil() {}

    public static Map<String, String> resolveClientIpInfo() {

        Map<String, String> info = new LinkedHashMap<>();

        try {
            HttpServletRequest req =
                (HttpServletRequest) FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequest();

            String ip = null;
            String source = "REMOTE_ADDR";

            for (String h : IP_HEADERS) {
                String v = req.getHeader(h);
                if (v != null && !v.isBlank()) {
                    ip = v.split(",")[0].trim();
                    source = h;
                    break;
                }
            }

            if (ip == null) {
                ip = req.getRemoteAddr();
            }

            InetAddress addr = InetAddress.getByName(ip);

            info.put("ip", ip);
            info.put("source", source);
            info.put("hostname", addr.getHostName());
            info.put("canonicalHost", addr.getCanonicalHostName());
            info.put("loopback", String.valueOf(addr.isLoopbackAddress()));
            info.put("private", String.valueOf(addr.isSiteLocalAddress()));
            info.put("multicast", String.valueOf(addr.isMulticastAddress()));

            if (addr instanceof Inet4Address) {
                info.put("version", "IPv4");
            } else if (addr instanceof Inet6Address) {
                info.put("version", "IPv6");
                info.put("ipv6Scope", ((Inet6Address) addr).getScopeId() + "");
            } else {
                info.put("version", "Unknown");
            }

        } catch (Exception e) {
            info.put("error", e.getMessage());
        }

        return info;
    }
}
