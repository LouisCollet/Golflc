
package listeners;

import static interfaces.Log.LOG;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;
// not used each object we want bound to the session as a session attribute needs to implement the HttpSessionBindingListener type independently. 
// @Named
//@WebListener() surtout pas ==> crash
//@ApplicationScoped
public class ImplHttpSessionBindingListener implements HttpSessionBindingListener {

    private String ihsblStr;

    // Constructor
    public ImplHttpSessionBindingListener(String ihsblStr) {
        this.ihsblStr = ihsblStr;
    }    

    @Override
    public void valueBound(HttpSessionBindingEvent event) { 
        LOG.debug("Within the session valueBound() method"); 
        // Get and write instance variable to console
        
        LOG.debug("Binding object:" + getIhsblStr() + " ,Name = " + event.getName());
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) { 
        LOG.debug("Within the session valueUnbound() method"); 
        // Get and write instance variable to console
        LOG.debug("Unbinding object:" + getIhsblStr());
    }

    // Getter and setter
    public String getIhsblStr() {
        return ihsblStr;
    }    

    public void setIhsblStr(String ihsblStr) {
        this.ihsblStr = ihsblStr;
    }    
}