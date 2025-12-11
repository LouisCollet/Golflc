
package rest;

/*/
http://www.mastertheboss.com/jboss-frameworks/resteasy/resteasy-tutorial
https://github.com/fmarchioni/mastertheboss/tree/master/jax-rs/basic

Before deploying your application, you need a JAX-RS class activator, which is a class extending javax.ws.rs.core.Application
and declaring the Path where JAX-RS Services will be available:
If the application WAR contains an Application class (or a subclass thereof) which is annotated with an ApplicationPath annotation, 
a web.xml file is not required. If the application WAR contains an Application class but the class doesn’t have a declared @ApplicationPath annotation,
then the web.xml must at least declare a servlet-mapping element.
*/
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/rest")
    public class RestActivator extends Application {
    //https://rob-ferguson.me/getting-started-with-resteasy/
    // The getSingletons() method returns a list of preallocated (i.e., objects that you have created) JAX-RS web services and providers.
    // The RESTEasy runtime will iterate through the list of objects and register them internally.
    // When the objects are registered, RESTEasy will also inject values for @Context annotated fields and setter methods.
    /*
     @Override
  public Set<Object> getSingletons() {
	HashSet<Object> set = new HashSet<Object>();
	set.add(new Controllers.CreditcardController());  // new 14-08-2025 sert à quoi ?? classes marqué avec @Path("creditcardController")
    return set;
  }
    */

}