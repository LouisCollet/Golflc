package utils;

import java.io.File;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import lc.golfnew.Constants;

public final class ELFunctions implements interfaces.GolfInterface, interfaces.Log
{

private ELFunctions()
{
        // Hide constructor.
}

//public static boolean contains(Collection<Object> collection, Object item)
//    {
//        return collection.contains(item);
//    }

public static String display(String s)
{// TESTING ONLY
LOG.info(" -- message from function display = " + s);
    //return s.toUpperCase();
    return (s == null ? "" : s.toUpperCase() );
}

public static String mdate (final String fake)
{   // File Modification Date (affiché dans footer.xhtml pour views
    //log.info(" -- message from function : entering mdate");
FacesContext ctx = FacesContext.getCurrentInstance();
HttpServletRequest sr = (HttpServletRequest) ctx.getExternalContext().getRequest();
String uri = sr.getRequestURI();
File file = new File(Constants.AP_TARGET + uri);  // ap from GolfInterface.java
return "File modification Date = " + SDF_TIME.format(file.lastModified())
      //  + " for file = " + file.toString()
        + " for uri = " + uri;
} //end method

} // end class