/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.omnifaces.util.Messages;

/**
 *https://www.developer.com/java/data/cross-field-validation-in-jsf.html
 * @author Collet
 */
@Named
@RequestScoped
public class ContactBean implements Serializable {

   private static final long serialVersionUID = 1L;

//   @ValidContact(message = "The name should be used in e-mail as name@domain.com!");
   private String name;

 //  @ValidContact(message = "The e-mail should be of type name@domain.com!");
   private String email;

   public String getName() {
        return name;
   }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    // getters and setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void someAction() {
        // OmniFaces approach
        Messages.addGlobalInfo("Thank you for your contacts!");
    }
}
