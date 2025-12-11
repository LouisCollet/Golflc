
package startup;

import static interfaces.Log.LOG;
import java.util.TimeZone;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
// import jakarta.faces.annotation.FacesConfig;  enlevé JSF4
import static utils.LCUtil.showMessageFatal;
@Startup
@Singleton
@DependsOn("StartupBean")

//  30-12-2018   https://rieckpil.de/howto-simple-form-based-authentication-for-jsf-2-3-with-java-ee-8-security-api/

/*https://eclipse-ee4j.github.io/mojarra/
https://github.com/eclipse-ee4j/mojarra/blob/2.3/README.md
There is currently only one way to activate CDI in Jakarta Faces 2.3 and herewith make Jakarta Faces 2.3
to run in full Jakarta Faces 2.3 modus.
Put the @FacesConfig annotation on an arbitrary CDI managed bean. For example, a general startup/configuration bean.
*/

//@FacesConfig supprimé 30-07-2023 plus nécessaire avec JSF 4
public class PostStartupBean {
  @PostConstruct
  public void init(){
   try{
  //     LOG.debug("@FacesConfig - CDI activated !!");
       LOG.debug("from within Wildfly - entering PostStartupBean - init ...");
   TimeZone.setDefault(TimeZone.getTimeZone("Europe/Brussels"));
       LOG.debug("TimeZone setted to = " + TimeZone.getDefault());
   // 14-04-2020 initialisation des settings ! important !!
       entite.Settings.init();

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
} //end class