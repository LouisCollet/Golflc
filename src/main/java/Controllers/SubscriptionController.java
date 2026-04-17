package Controllers;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("subscriptionC")
@SessionScoped
public class SubscriptionController implements Serializable {

    private static final long serialVersionUID = 1L;

    public SubscriptionController() { }

    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
