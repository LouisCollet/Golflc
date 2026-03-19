
package info_test;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Map;

@Named("infoView")
@RequestScoped
public class InfoView {

    @Inject InfoService3 infoService;

    public Map<String, String> getInfo() {
        return infoService.technicalInfo();
    }
}
