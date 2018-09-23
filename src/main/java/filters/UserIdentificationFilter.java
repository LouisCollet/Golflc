/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filters;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.ThreadContext;
////import org.slf4j.MDC; mod 27/08/2014

/**
 * An example authentication filter which is used to intercept all the requests
 * for fetching the user name from it and put the user name to the Log4j Mapped
 * Diagnostic Context (MDC), so that the user name could be used for
 * differentiating log messages.
 *
 * @author veerasundar.com/blog
 * new 29/08/2014
 *http://logging.apache.org/log4j/2.x/manual/eventlogging.html
 */
@WebFilter(filterName="UserIdentificationFilter", urlPatterns = "/*")
public class UserIdentificationFilter implements Filter, interfaces.Log
{
private final Set<String> localAddresses; 
// if it is running in a IPv6 machine, the address would be 0:0:0:0:0:0:0:1.

    public UserIdentificationFilter()
    {
        this.localAddresses = new HashSet<>();
    }

@Override
public void init(FilterConfig config) throws ServletException {
    try {
        localAddresses.add(InetAddress.getLocalHost().getHostAddress());
        for (InetAddress inetAddress : InetAddress.getAllByName("localhost"))
        {
            localAddresses.add(inetAddress.getHostAddress());
        }
    } catch (UnknownHostException e)
    {
        throw new ServletException("Unable to lookup local addresses = " + e);
    }
}
@Override
public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain chain) throws IOException, ServletException
    {
        if (localAddresses.contains(servletRequest.getRemoteAddr()))
        {
           // LOG.info("contains remoteadr = " + localAddresses);
           // LOG.info("   = " + localAddresses.toArray()[0] );
                    //contains remoteadr = [192.168.1.8, 0:0:0:0:0:0:0:1, 127.0.0.1]
        }else{
            LOG.info("contains NOT remoteadr = " + localAddresses);
    }

            String ip = localAddresses.toArray()[0].toString();
            //String rh = request..getRemoteHost();
        try {
            /* il ne faut pas adapter web.xml si on utilise webfilter !!!
             * This code puts the value "userName" to the Mapped Diagnostic
             * context. Since MDC is a static class, we can directly access it
             * with out creating a new object from it. Here, instead of hard
             * coding the user name, the value can be retrieved from a HTTP
             * Request object.
             */

        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        ThreadContext.put("remotepAddress", request.getRemoteAddr());
        HttpSession session = request.getSession(false);
 ///           MDC.put("remoteAddress", ip);
            ThreadContext.put("ipAddress", ip); // new 29/08/2014
  //          ThreadContext.put("remoteAddress", request.getRemoteAddr());
  //         ThreadContext.put("hostName", request.getServerName()); // toujours "localhost"

            
            
         //   HttpServletRequest containerRequest = (HttpServletRequest)cycle.getRequest().getContainerRequest();
         //   MDC.put("serverName", containerRequest.getServerName());
         //   MDC.put("sessionId",  containerRequest.getSession().getId());

//            MDC.put("remoteHost", rh);
            chain.doFilter(request, response);

        }
        finally
        {
           // MDC.remove("remoteAddress");
         //   MDC.clear();
           ThreadContext.clearAll();

        }

    }
    
@Override
   public void destroy()
   {
       //fc = null;
   }

} //end class
