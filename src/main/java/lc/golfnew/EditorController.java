/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

import static interfaces.Log.LOG;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;


@Named("editorC")
@SessionScoped 
public class EditorController implements Serializable{
     
    private String text;
    private String text2;
    private String content;  
     private String color = "#33fc14";  
     
    public EditorController() {  
        content = "Hi Showcase User";  
     //   secondContent = "This is a second editor";  
    }  

    
    
    public void saveListener() {  
        content = content.replaceAll("\\r|\\n", "");  
          final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Content",  
                    content.length() > 150 ? content.substring(0, 100) : content);  
        FacesContext.getCurrentInstance().addMessage(null, msg);  
    }  
    public void changeColor() {  
        if (color.equals("#1433FC")) {  
            color = "#33fc14";  
        } else {  
            color = "#1433FC";  
        }  
    }  

    public String getText() {
         LOG.info("getText = " + text);
        return text;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
 
    public void setText(String text) {
        LOG.info("setText = " + text);
        this.text = text;
    }
 
    public String getText2() {
        return text2;
    }
 
    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    
}