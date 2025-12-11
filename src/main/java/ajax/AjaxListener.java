
package ajax;

import static interfaces.Log.LOG;
import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;
// https://www.journaldev.com/3291/primefaces-ajaxbehavior-and-ajaxexceptionhandler-component-example-tutorial
/*
bloc 1
comment line 1
comment line 2
*/
@Named("ajaxListener")
@SessionScoped
public class AjaxListener implements Serializable{ // partial comment
	private String message = "";

	public String getMessage() {
		return message;
	}
/*
bloc2
comment line 3
comment line 4
comment line 5
*/
	public void setMessage(String message) {
		this.message = message;
	}

	public void keyUpListener(AjaxBehaviorEvent e){
		LOG.debug("AjaxListener :: "+ e.getBehavior()+ " :: " 
                        + e.getSource() + " :: "+ e.getComponent());
	}
}