
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

@FacesConfig 

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