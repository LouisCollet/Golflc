
package lc.golfnew;

import static interfaces.Log.LOG;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import utils.LCUtil;
// https://javabeat.net/jsf-custom-error-pages/
//https://kahimyang.com/kauswagan/code-blogs/997/custom-error-pages-for-tomcat-jsf-applications
//https://stackoverflow.com/questions/18258085/custom-error-page-get-originally-requested-url

@Named("errorHandler") // this qualifier  makes a bean EL-injectable (Expression Language)
@SessionScoped
public class ErrorHandler implements Serializable{
    

	public String getStatusCode(){
		String val = String.valueOf((Integer)FacesContext.getCurrentInstance().getExternalContext().
			getRequestMap().get("javax.servlet.error.status_code"));
		return val;
	}

	public String getMessage(){
		String val =  (String)FacesContext.getCurrentInstance().getExternalContext().
			getRequestMap().get("javax.servlet.error.message");
		return val;
	}

  
	public String getExceptionType(){
try{          
 //String urlException = FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("javax.servlet.error.request_uri").toString();
//LOG.info("url exception = " + urlException);
    LOG.info("entering getExceptionType");
		String val = FacesContext.getCurrentInstance().getExternalContext().
			getRequestMap().get("javax.servlet.error.exception_type").toString();
		return val;
        } catch (Exception e) {
            String msg = "£££ Exception in getExceptionType = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
} //end method
        } // 
	public String getException(){
		String val = ((Exception)FacesContext.getCurrentInstance().getExternalContext().
			getRequestMap().get("javax.servlet.error.exception")).toString();
		return val;
	}

	public String getRequestURI(){
		return (String)FacesContext.getCurrentInstance().getExternalContext().
			getRequestMap().get("javax.servlet.error.request_uri");
	}

	public String getServletName(){
		return (String)FacesContext.getCurrentInstance().getExternalContext().
			getRequestMap().get("javax.servlet.error.servlet_name");
	}

public String getStackTrace() {
 // à implémenter !!
    FacesContext context = FacesContext.getCurrentInstance();
    Map<?,?> requestMap = context.getExternalContext().getRequestMap();
    Throwable ex = (Throwable) requestMap.get("javax.servlet.error.exception");
     StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    return sw.toString();

 }
} //end class ErrorHandler