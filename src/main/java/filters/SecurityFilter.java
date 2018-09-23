package filters;

// dont!t forget web.xml !!

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SecurityFilter implements Filter
{
      FilterConfig fc;

@Override
      public void init(FilterConfig filterConfig)throws ServletException
   {
      fc = filterConfig;
   }
   
@Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
   throws IOException, ServletException
{
      HttpServletRequest req = (HttpServletRequest)request;
      HttpServletResponse resp = (HttpServletResponse) response;
      HttpSession session = req.getSession(true);

      String pageRequested = req.getRequestURI().toString();
      if(session.getAttribute("user") == null && !pageRequested.contains("login.xhtml"))
      {
         resp.sendRedirect("login.xhtml");
      }else{
        chain.doFilter(request, response);
      }
} //end method
   

@Override
   public void destroy()
   {
       fc = null;
   }

} //end class