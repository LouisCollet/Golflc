
package listeners;

/*
 *http://www.hubberspot.com/2013/09/how-to-create-listener-using.html
 * https://www.digitalocean.com/community/tutorials/servletcontextlistener-servlet-listener-example
 * @author Collet 
 */
import static interfaces.Log.LOG;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.SQLException;
import static utils.LCUtil.showMessageFatal;

@WebListener
public class StartStopAppListener implements ServletContextListener {
   private Connection conn = null; 
 @Override
 public void contextInitialized(ServletContextEvent event) {  // is invoked when application is deployed on the server.
  LOG.debug("Application deployed on the server - Servlet Context Initialized ... " + this);
 //storing connection object as an attribute in ServletContext  
 
try{
    ServletContext context = event.getServletContext();  
    context.setAttribute("playerid", "");  
    context.setAttribute("playerlastname", "");  
    context.setAttribute("playerage", 0);  
  //  context.setAttribute("playerlastname", "");  
    context.setAttribute("creditcardType", "INITIALIZED");  
    LOG.debug("creditcardtype attribute = " + context.getAttribute("creditcardType"));
    conn = new utils.DBConnection().getConnection();
    context.setAttribute("Connection",conn); 
  //     sessionMap.put("playerage", 0);
        // 15-10-2020 initialisation de la map
 ///      sessionMap.put("inputSelectHomeClub", "login");
     LOG.debug("attribute Connection application or session ? =  " + context.getAttribute("Connection"));
//   FacesContext.getCurrentInstance()
//            .getExternalContext().getApplicationMap().get("creditcardType")); 
        LOG.debug("Server info = " + context.getServerInfo());
    LOG.debug("The default session tracking modes: " + event.getServletContext().getDefaultSessionTrackingModes());    
  
    
  //  event.getServletContext().setAttribute(FOO, myObject);
  }catch (SQLException e){
    String msg = "SQLException in Opening Connection : \n<br/>" + "\nErrorcode = " + e;
	LOG.error(msg);
        showMessageFatal(msg);
      //  return null;  
  }catch (Exception ex){
        String msg = "Exception in Opening Connection " + ex;
        LOG.error(msg);
        showMessageFatal(msg);
        //    return null;
  }
 } // end method
 
 //https://stackoverflow.com/questions/24628744/when-is-contextdestroyed-called
  @Override
 public void contextDestroyed(ServletContextEvent servletContextEvent) {  //  is invoked when application is undeployed from the server.
  LOG.debug("Application undeployed from the server - Servlet Context Destroyed ... ");
 // https://stackoverflow.com/questions/315073/jsf-initialize-application-scope-bean-when-context-initialized
 // close database 
 try{
    ServletContext ctx = servletContextEvent.getServletContext();
    //	DBConnectionManager dbManager = (DBConnectionManager) ctx.getAttribute("DBManager");
    // a adapter: trouver conn
        utils.DBConnection.closeQuietly(conn, null, null , null); 
    //	dbManager.closeConnection();
    	LOG.debug(" Database connection closed for Application ?");
    }catch (SQLException e){
   String msg = "SQLException in Opening Connection : \n<br/>" + "\nErrorcode = " + e;
	LOG.error(msg);
        showMessageFatal(msg);
      //  return null;
    }
  } // end method
 }  //end class