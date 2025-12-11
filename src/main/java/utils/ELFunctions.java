package utils;

import static interfaces.Log.LOG;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

public final class ELFunctions implements interfaces.GolfInterface{
// why final ?
private ELFunctions(){
        // Hide constructor.
}

//public static boolean contains(Collection<Object> collection, Object item)
//    {
//        return collection.contains(item);
//    }

public static String display(String s){
// TESTING ONLY
LOG.debug(" -- message from function display = " + s);
    //return s.toUpperCase();
    return (s == null ? "" : s.toUpperCase() );
}
// d'autres fonctions peuvent venir ici !!!
// exemple un string année début-année fin



public static String mdate (final String fake)
{   // File Modification Date (affiché dans footer.xhtml pour views
    //log.info(" -- message from function : entering mdate");
FacesContext ctx = FacesContext.getCurrentInstance();
HttpServletRequest sr = (HttpServletRequest) ctx.getExternalContext().getRequest();
String uri = sr.getRequestURI();

// enlevé 25-03*2020
// File file = new File(Constants.AP_TARGET + uri);  // ap from GolfInterface.java

return "";
//return "File modification Date = " + SDF_TIME.format(file.lastModified())
      //  + " for file = " + file.toString()
   //     + " for uri = " + uri;
} //end method

} // end class