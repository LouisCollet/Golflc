
package listeners;
import edu.emory.mathcs.backport.java.util.Arrays;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.Log.LOG;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
// http://www.xyzws.com/Servletfaq/how-to-count-active-sessions-in-your-web-application/11
@Named
@WebListener()
@ApplicationScoped
public class MySessionCounter implements HttpSessionListener {
  private static int activeSessions = 0;
  public static final String COUNTER = "session-counter";
  private List<String> sessions = new ArrayList<>();

  @Override
  public void sessionCreated(HttpSessionEvent se){
      HttpSession session = se.getSession();
      sessions.add(session.getId());
      session.setAttribute(MySessionCounter.COUNTER, this);
        LOG.info(" (session) Created: " + session.getId());
        LOG.info(" MaxInactiveInterval = " + session.getMaxInactiveInterval());
   //     LOG.info(" (session) Created at = " + session.getCreationTime());  // long
        Date result = new Date(session.getCreationTime()); 
        LOG.info("session created at : " + SDF_TIME.format(result)); 
        LOG.info(" after add sessions are now : " + Arrays.deepToString(sessions.toArray()));
    activeSessions++;
    LOG.info("session created, number of activeSessions = " + activeSessions); 
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
      HttpSession session = se.getSession();
      Date result = new Date(session.getCreationTime()); 
        LOG.info("session created at : " + SDF_TIME.format(result)); 
        LOG.info("sessionDestroyed:Id:" + se.getSession().getId());
        LOG.info("destroy time = " + LocalDateTime.now());
      sessions.remove(session.getId());
      session.setAttribute(MySessionCounter.COUNTER, this);
      LOG.info(" after remove sessions are now : " + Arrays.deepToString(sessions.toArray()));
    if(activeSessions > 0){
      activeSessions--;}
     LOG.info("session destroyed, number of activeSessions = " + activeSessions); 
  }

  public void sessionInvalidate(HttpSessionEvent se) {
    LOG.info("HttpSessionEvent session invalidated = " + se.toString());//+ activeSessions); 
  }
  public int getActiveSessions() {
   LOG.info("number of active sessions = " + activeSessions);
///   LOG.info("number of active sessions 2 = " + sessions.size());
   return activeSessions;
  }
} // end class