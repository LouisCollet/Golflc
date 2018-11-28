/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;

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
