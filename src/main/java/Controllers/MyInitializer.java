
package Controllers;

import static interfaces.Log.LOG;
import java.util.Set;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;

 public class MyInitializer implements ServletContainerInitializer {
       @Override
       public void onStartup(Set<Class<?>> c, ServletContext cx) {
           LOG.debug("--- Servlet CONTAINER INITIALIZER! ---");
       }
    } //end class