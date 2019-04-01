
package lc.golfnew;

import static interfaces.Log.LOG;
import java.sql.Connection;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.annotation.FacesConfig;
import javax.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import utils.DBConnection;
@Startup
@Singleton
@DependsOn("StartupBean")
@ApplicationScoped

//  30-12-2018   https://rieckpil.de/howto-simple-form-based-authentication-for-jsf-2-3-with-java-ee-8-security-api/
/*
@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage="login_securityAPI.xhtml",
                errorPage="error_securityAPI.xhtml" // DRAFT API - must be set to empty for now
        )
)
*/
//To register a custom login page, you have to inform the authentication mechanism about 
//the name and location of your login page. 
//This can be done programmatically with a simple configuration class:

@CustomFormAuthenticationMechanismDefinition( 
        loginToContinue = @LoginToContinue( 
             //   loginPage = "/login.xhtml",
                 loginPage="/login_securityAPI.xhtml",
                 errorPage="/error_securityAPI.xhtml",
 //The property useForwardToLogin is set to false to use a redirect instead of a forward. 
                 useForwardToLogin = false 
            ) 
) 
/*
Activating CDI in JSF 2.3
By default, JSF 2.3 will run in JSF 2.2 modus as to CDI support. Even when you use a JSF 2.3 compatible faces-config.xml.
In other words, the new JSF 2.3 feature of injection and EL resolving of JSF artifacts 
(spec issue 1316) won't work until you explicitly activate this.
In other words, @Inject FacesContext doesn't work by default.
This is necessary in order for JSF 2.3 to be fully backwards compatible.
There is currently only one way to activate CDI in JSF 2.3 and herewith make JSF 2.3 to run in full JSF 2.3 modus.
Put the @FacesConfig annotation on an arbitrary CDI managed bean.
For example, a general startup/configuration bean.
*/

//@FacesConfig 
@FacesConfig(version = FacesConfig.Version.JSF_2_3)
public class PostStartupBean {
   private static Connection conn = null;
   private static Connection connPool = null;
  @PostConstruct
  public void init(){
   try{
       LOG.info("Wildfly - entering PostStartupBean - init ...");    
////       PostStartupBean psb = new PostStartupBean();

  // ici open database : la seule pour toute la session ! fonctionne !!
////   utils.DBConnection dbc = new utils.DBConnection();
////    conn = dbc.getConnection();
//        LOG.info("classic connection 1 = " + conn);




// configure UNE fois la datasource    !!!!!
   utils.DBConnection.setDataSource();
   connPool = DBConnection.getPooledConnection(); // utilisée pour listAllSystemProperties !! contraire au principe de datasource !!!
 //       LOG.info("pooled connection 2 = " + connPool);
   /// LOG.info("line 02");
   TimeZone.setDefault(TimeZone.getTimeZone("Europe/Brussels"));
   // enlevé provisoirement 
   
////  utils.LCUtil.ListAllSystemProperties();
////  utils.DBMeta.listMetaData(connPool);
//   security.CustomIdentityStore.;

    LOG.info("exiting PostStartupBean");
  }catch (Exception e){
	LOG.error("Fatal Exception in PostStartupBean : "  + e);
}
  } //end method init

} //end class