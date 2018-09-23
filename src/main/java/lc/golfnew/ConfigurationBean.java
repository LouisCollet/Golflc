
package lc.golfnew;
import static interfaces.Log.LOG;
import javax.faces.annotation.FacesConfig;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 * https://stackoverflow.com/questions/45846590/how-to-inject-facescontext-with-jsf-2-3-and-tomee
 * Seems like some JSF2.3 features must be activated by setting the used JSF version.
 * Try setting JSF version by adding this empty class:
 * The presence of the @FacesConfig annotation on a managed bean deployed within an application enables version specific
 * features. In this case, it enables JSF CDI injection and EL resolution using CDI.
 *
 */
@FacesConfig(version = FacesConfig.Version.JSF_2_3)

public class ConfigurationBean {
    @Inject
   private FacesContext facesContext;
    public void init(){
        
        LOG.info("from empty ConfigurationBean - injected FacesContext JSF_2_3 = " + facesContext.toString());
    }
}