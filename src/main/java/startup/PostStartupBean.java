
package startup;

import static interfaces.Log.LOG;
import java.util.TimeZone;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
// import jakarta.faces.annotation.FacesConfig;  enlevé JSF4
import static utils.LCUtil.showMessageFatal;
@Startup
@Singleton
@DependsOn("StartupBean")

//@FacesConfig supprimé 30-07-2023 plus nécessaire avec JSF 4
public class PostStartupBean {
    @Inject private entite.Settings settings;       // ✅ force init de Settings
     // Dans PostStartupBean — l'injection force l'instanciation de Settings
  @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        
        try {
            // ✅ TimeZone par défaut
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Brussels"));
            LOG.debug(methodName + " - TimeZone set to = " + TimeZone.getDefault());
            LOG.debug(methodName + " - Settings already initialized: " + settings.getProperty("EXECUTION"));
            // ✅ Settings.init() supprimé — @PostConstruct dans Settings CDI s'en charge
            // ❌ entite.Settings.init();    → plus nécessaire
            // ❌ settings.Settings.init();  → plus nécessaire
            LOG.debug(methodName + " - exiting PostStartupBean");
        } catch (Exception e) {
            String msg = "Fatal Exception in " + methodName + " : " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
        }
    } // end method
  
  /*
  public void init(){
   try{
  //     LOG.debug("@FacesConfig - CDI activated !!");
       LOG.debug("from within Wildfly - entering PostStartupBean - init ...");
   TimeZone.setDefault(TimeZone.getTimeZone("Europe/Brussels"));
       LOG.debug("TimeZone setted to = " + TimeZone.getDefault());
   // 14-04-2020 initialisation des settings ! important !!
       entite.Settings.init();
       // new 31-12-2025
       settings.Settings.init();

   // activer pour debugging
//    Controllers.InfoController.ListAllSystemProperties();
    
// new 01-12-2021 activer !!
   //Controllers.SchedulerController sc = new Controllers.SchedulerController();
   //sc.run();
   //sc.list();

    LOG.debug("exiting PostStartupBean");
  }catch (Exception e){
      String msg = "Fatal Exception in PostStartupBean : "  + e;
	LOG.error(msg);
        showMessageFatal(msg);
}
  } //end method init
  */
  
} //end class