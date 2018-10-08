
package lc.golfnew;

import static interfaces.Log.LOG;
import java.sql.Connection;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import utils.DBConnection;
@Startup
@Singleton
@DependsOn("StartupBean")
//@ApplicationScoped
public class PostStartupBean {
   private static Connection conn = null;
   private static Connection connPool = null;
  @PostConstruct
  public void init(){
   try{
       LOG.info("Wildfly - entering PostStartupBean - init ...");    
////       PostStartupBean psb = new PostStartupBean();
  // ici open database : la seule pour toute la session !
    utils.DBConnection dbc = new utils.DBConnection();
    conn = dbc.getConnection();
        LOG.info("classic connection 1 = " + conn);
   // configure UNE fois la datasource    !!!!!
   utils.DBConnection.setDataSource();
   connPool = DBConnection.getPooledConnection();
        LOG.info("pooled connection 2 = " + connPool);
   /// LOG.info("line 02");
   TimeZone.setDefault(TimeZone.getTimeZone("Europe/Brussels"));
   utils.LCUtil.ListAllSystemProperties();
  // 
   utils.DBMeta.listMetaData(connPool);
//   security.CustomIdentityStore.;
   
   
    LOG.info("exiting PostStartupBean");
  }catch (Exception e){
	LOG.error("Fatal Exception in PostStartupBean : "  + e);
}
  } //end method init
//  ...
    public static Connection getConn() {
        return conn;
    }
    public static Connection getConnPool() {
        return connPool;
    }
} //end class