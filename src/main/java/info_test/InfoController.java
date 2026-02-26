
package info_test;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("infoController")
@RequestScoped
public class InfoController {

    @Inject
    InfoService info;

    public String getMysql() { return info.mysql(); }
    public String getMongo() { return info.mongo(); }
    public String getIp()    { return info.ip(); }
    public String getGeo()   { return info.geo(); }
}
