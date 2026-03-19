package lc.golfnew;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;

@Named
@SessionScoped
public class AjaxStatus implements Serializable{
	private String message = "";

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void listener(AjaxBehaviorEvent e) throws Exception{
		Thread.sleep(5000);
		System.out.println(e);
	}

	public String printMessage(){
		return "";
	}
}