
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
   private static Connection conn2 = null;
  @PostConstruct
  public void init(){
   try{
       LOG.info("Wildfly - entering PostStartupBean - init ...");    
////       PostStartupBean psb = new PostStartupBean();
  // ici open database : la seule pour toute la session !
    utils.DBConnection dbc = new utils.DBConnection();
    conn = dbc.getConnection();
        LOG.info("classic connection 1 = " + conn);
   
    conn2 = utils.DBConnection.getPooledConnection(); // pooled
        LOG.info("pooled connection 2 = " + conn2);
  }catch (Exception e){
	LOG.error("Fatal Exception in PostStartupBean : "  + e);
}
  } //end method init
//  ...
    public static Connection getConn() {
        return conn;
    }
    public static Connection getConn2() {
        return conn2;
    }
    
    
} //end class