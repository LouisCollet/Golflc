/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listeners;

/**
 *http://www.hubberspot.com/2013/09/how-to-create-listener-using.html
 * @author Collet 
 */
import static interfaces.Log.LOG;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
@Named
@ApplicationScoped
@WebListener
public class StartStopAppListener implements ServletContextListener {
 
 public void contextInitialized(ServletContextEvent event) {  // is invoked when application is deployed on the server.
  LOG.info("Application deployed on the server - Servlet Context Initialized ... " + this);
 //storing connection object as an attribute in ServletContext  
    ServletContext ctx = event.getServletContext();  
    ctx.setAttribute("mycon", "??con");  
        LOG.info("server info = " + ctx.getServerInfo());
    LOG.info("The default session tracking modes: " +
                            event.getServletContext()
                                 .getDefaultSessionTrackingModes());    
  
 }
 
 public void contextDestroyed(ServletContextEvent servletContextEvent) {  //  is invoked when application is undeployed from the server.
  LOG.info("Application undeployed from the server - Servlet Context Destroyed ... ");
  }
 }  //end class