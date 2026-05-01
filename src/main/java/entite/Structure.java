package entite;

import com.fasterxml.jackson.annotation.JsonInclude;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static utils.LCUtil.showMessageFatal;

// utilisé dans ControllerUnvailable
public class Structure{
  private Integer courseId;
  private String item;
  private Boolean status;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String comment;

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

  public String toString(){
 try {
//      LOG.debug("starting toString TarifGreenfee !");
    return
            ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
              + " courseId='" + courseId
              + ", item=" + item
              + ", status=" + status
            );
 }catch(Exception e){
    String msg = "£££ Exception in Structure.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
} //end method
} // end class Structure