package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import utils.LCUtil;

public class Course implements Serializable, interfaces.Log{
    private static final long serialVersionUID = 1L;

@NotNull(message="{course.id.notnull}")
@Min(value=1, message="{course.id.min}")
    private Integer idcourse;

@Pattern(regexp = "[a-zA-Z0-9'âéèàê ç-]*",message="{course.name.characters}")
@NotNull(message="{course.name.notnull}")
@Size(max=45,message="{course.name.size}")

    private String courseName;

@NotNull(message="{course.holes.notnull}")
@Size(min=1,max=2,message="Bean validation : the Round Game is min 3, max 20 characters")
@Min(value=9,message="{tee.slope.min}")
@Max(value=18,message="{tee.slope.max}")
    private short courseHoles;

@NotNull(message="{course.par.notnull}")
@Min(value=69,message="{tee.slope.min}") // still used ??
@Max(value=74,message="{tee.slope.max}") // still used ??
    private Short coursePar;
// new 19-08-2023
 //   private Integer club_idclub;
    private int club_idclub;  // mod 15-02-026
  //  private Date courseBeginDate;
    private LocalDateTime courseBeginDate;
  //  private Date courseEndDate;
    private LocalDateTime courseEndDate;
 //   private Date courseModificationDate;
    private boolean NextCourse; // 23/06/2013
    private boolean CreateModify = true; // 12/08/2017
//002-04    private String inputSelectCourse; // new 27/08/2018  
    public Course()    {
       courseHoles = 18; //set default value to 18 in radiobutton
       coursePar = (short)72;
    }

    public Integer getIdcourse() {
        return idcourse;
    }

    public void setIdcourse(Integer idcourse) {
        this.idcourse = idcourse;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public short getCourseHoles() {
        return courseHoles;
    }

    public void setCourseHoles(short courseHoles) {
        this.courseHoles = courseHoles;
    }

    public Short getCoursePar() {
   //      LOG.debug("getCoursepar = " + coursePar);
        return coursePar;
    }

    public void setCoursePar(Short coursePar) {
        this.coursePar = coursePar;
 //       LOG.debug("setCoursepar to " + this.coursePar);
    }

    public int getClub_idclub() {
        return club_idclub;
    }

    public void setClub_idclub(int club_idclub) {
        this.club_idclub = club_idclub;
    }

 

 //   public Date getCourseModificationDate() {
 //       return courseModificationDate;
 //   }

    public LocalDateTime getCourseBeginDate() {
        return courseBeginDate;
    }

    public void setCourseBeginDate(LocalDateTime courseBeginDate) {
        this.courseBeginDate = courseBeginDate;
    }

    public LocalDateTime getCourseEndDate() {
        return courseEndDate;
    }

    public void setCourseEndDate(LocalDateTime courseEndDate) {
        this.courseEndDate = courseEndDate;
    }


//    public void setCourseModificationDate(Date courseModificationDate) {
//        this.courseModificationDate = courseModificationDate;
//    }

    public boolean isNextCourse() {
        return NextCourse;
    }

    public void setNextCourse(boolean NextCourse) {
        this.NextCourse = NextCourse;
    }

    public boolean isCreateModify() {
        return CreateModify;
    }

    public void setCreateModify(boolean CreateModify) {
        this.CreateModify = CreateModify;
    }

 @Override
public String toString(){
    try{
       // LOG.debug("starting toString Course !");
    return 
        ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE 
          + " idcourse : "   + this.getIdcourse()
          + " ,course Name : " + this.getCourseName()
          + " ,course Par : " + this.getCoursePar()
          + " ,course Holes : " + this.getCourseHoles()
          + " ,Begin course = " + this.getCourseBeginDate()
          + " ,End course = " + this.getCourseEndDate()
   //       + " ,inputSelectCourse = " + this.getInputSelectCourse()
        );
        }catch(Exception e){
        String msg = "£££ Exception in Course.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}
} // end class