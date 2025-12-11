
package listeners;

import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
// http://www.xyzws.com/Servletfaq/how-to-count-active-sessions-in-your-web-application/11
//@Name



@WebListener()
//@ApplicationScoped enlevé 11-03-2024
 //@Retention(value=RUNTIME)
public class ImplHttpSessionListener implements HttpSessionListener {
  private static int activeSessions = 0;
  private static final String COUNTER = "session-counter";
  
  private List<String> sessions = new ArrayList<>();


  
  @Override
  public void sessionCreated(HttpSessionEvent event){
      HttpSession session = event.getSession();
         LOG.debug(" sessionId Created: " + session.getId());
      sessions.add(session.getId());
      session.setAttribute(ImplHttpSessionListener.COUNTER, this);
         LOG.debug(" contextpath : " + session.getServletContext().getContextPath());
     //   LOG.debug(" servlet version : " + session.getServletContext().getMajorVersion());
      //  LOG.debug(" context path : " + session.getServletContext().getContextPath());
    //    LOG.debug(" servlet context name : " + session.getServletContext().getServletContextName());
     //   LOG.debug(" MaxInactiveInterval = " + session.getMaxInactiveInterval());
        LocalDateTime ldt = Instant.ofEpochMilli(session.getCreationTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LOG.debug("session created at localdatetime : " + ldt.format(ZDF_TIME)); 
        LOG.debug(" after add, sessions are now : " + Arrays.deepToString(sessions.toArray()));

    Enumeration<String> attributes = session.getAttributeNames();
    while (attributes.hasMoreElements()) {
        String attribute = attributes.nextElement();
        LOG.debug("AttributeNames = " + attribute + " / " + event.getSession().getAttribute(attribute));
    }
//}
    
    
    activeSessions++;
    LOG.debug("session created, number of activeSessions = " + activeSessions); 
    
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
      HttpSession session = se.getSession();
  //    Date result = new Date(session.getCreationTime()); 
        LOG.debug("sessionDestroyed  iD = " + session.getId());
       //  LOG.debug("session created at : " + SDF_TIME.format(new Date(session.getCreationTime())));
         LocalDateTime ldt = Instant.ofEpochMilli(session.getCreationTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LOG.debug("session was created at localdatetime : " + ldt.format(ZDF_TIME)); 
        LOG.debug("destroy time = " + LocalDateTime.now().format(ZDF_TIME));
      sessions.remove(session.getId());
      session.setAttribute(ImplHttpSessionListener.COUNTER, this);
//     LOG.debug(" after remove sessions are now : " + Arrays.deepToString(sessions.toArray()));
    if(activeSessions > 0){
      activeSessions--;
    }
     LOG.debug("session destroyed, number of remaining activeSessions = " + activeSessions); 
  }

  public void sessionInvalidate(HttpSessionEvent event) {
    LOG.debug("HttpSessionEvent session invalidated = " + event.toString());//+ activeSessions); 
  }
  public int getActiveSessions() {
   LOG.debug("number of active sessions = " + activeSessions);
///   LOG.debug("number of active sessions 2 = " + sessions.size());
   return activeSessions;
  }
} // end class