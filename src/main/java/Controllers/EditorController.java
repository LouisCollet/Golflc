
package Controllers;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("editorC")
@SessionScoped 
public class EditorController implements Serializable{
     
    private String text;
    private String text2;
    private String content;  
    private String color = "#33fc14";  
     
  public EditorController() {  // constructor
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
         LOG.debug("getText = " + text);
        return text;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
 
    public void setText(String text) {
        LOG.debug("setText = " + text);
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