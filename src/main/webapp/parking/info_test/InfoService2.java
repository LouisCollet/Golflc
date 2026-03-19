
package info_test;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

/**
 *
 * @author Louis Collet
 */
@ApplicationScoped
public class InfoService2 {
        public Map<String, String> clientIpInfo() {
        return ClientIpUtil.resolveClientIpInfo();
    }
}
