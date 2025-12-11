package entite.composite;

import entite.Club;
import entite.Course;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import utils.LCUtil;

@Named
@RequestScoped 
public class EClubCourse implements Serializable{
    //@Inject creates instance + initialize : pas nécessaire dans constructeur !
@Inject  private Course course;
@Inject  private Club club;

public EClubCourse(){  //No-args constructor
}

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

@Override
public String toString(){ 
 try{
//    LOG.debug("starting toString ECompetition !");
    return ( 
          NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE 
        + NEW_LINE + TAB
               + " ,vers Club : " + club
        + NEW_LINE + TAB
               + " ,vers Course : " + course
        );
  }catch(Exception e){
        String msg = "£££ Exception in EClubCourse.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class