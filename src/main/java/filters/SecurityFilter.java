package filters;

// dont't forget web.xml !!

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

      // ✅ Security headers — prevent clickjacking, MIME sniffing, XSS
      resp.setHeader("X-Content-Type-Options", "nosniff");
      resp.setHeader("X-Frame-Options", "SAMEORIGIN");
      resp.setHeader("X-XSS-Protection", "1; mode=block");
      resp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
      resp.setHeader("Permissions-Policy", "geolocation=(self), camera=(), microphone=()");

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
