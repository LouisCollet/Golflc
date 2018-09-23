
package lc.golfnew;

import static interfaces.Log.LOG;
import java.sql.Connection;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
@Startup
@Singleton
@DependsOn("StartupBean")
public class PostStartupBean {
   private static Connection conn = null;
  @PostConstruct
  public void init(){
   try{
       LOG.info("Wildfly - entering PostStartupBean - init ...");    
       PostStartupBean psb = new PostStartupBean();
  // ici open database : la seule pour toute la session !
    utils.DBConnection dbc = new utils.DBConnection();
    conn = dbc.getConnection();
  }catch (Exception e){
	LOG.error("Fatal Exception in PostStartupBean : "  + e);
}
  } //end method init
//  ...
    public static Connection getConn() {
        return conn;
    }
} //end class