package entite;

import static interfaces.GolfInterface.NEWLINE;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.inject.Named;
import javax.validation.constraints.*;
import utils.LCUtil;
//import javax.validation.constraints.Pattern;
//import javax.validation.constraints.Size;
/**
 *
 * @author collet
 */
@Named
public class Course implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;

@NotNull(message="Bean validation : the Course ID must be completed")
    private Integer idcourse;

@Pattern(regexp = "[a-zA-Z0-9'âéèàê ç-]*",message="{course.name.characters}")
@NotNull(message="{course.name.notnull}")
@Size(max=45,message="{course.name.size}")

    private String courseName;

@NotNull(message="{course.holes.notnull}")
    private short courseHoles;

@NotNull(message="{course.par.notnull}")
    private short coursePar;

//@NotNull(message="Bean validation : the Gender must be completed")
//    @Size(max = 1,message="Bean validation : the Gender is maximum 1 character")
//    private String CourseGender;

    private Integer club_idclub;

    private Date courseBegin;
    private Date courseEnd;
    private Date courseModificationDate;

    private boolean NextCourse; // 23/06/2013
    private boolean CreateModify = true; // 12/08/2017
    private String inputSelectCourse; // new 27/08/2018  
    public Course()
    {
       courseHoles = 18; //set default value to 18 in radiobutton
       coursePar=72;
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

    public short getCoursePar() {
        return coursePar;
    }

    public void setCoursePar(short coursePar) {
        this.coursePar = coursePar;
    }

 //   public String getCourseGender() {
 //       return CourseGender;
 //   }

 //   public void setCourseGender(String CourseGender) {
 //       this.CourseGender = CourseGender;
 //   }

    public Integer getClub_idclub() {
        return club_idclub;
    }

    public void setClub_idclub(Integer club_idclub) {
        this.club_idclub = club_idclub;
    }

    public Date getCourseModificationDate() {
        return courseModificationDate;
    }

    public Date getCourseBegin() {
        return courseBegin;
    }

    public void setCourseBegin(Date courseBegin) {
        this.courseBegin = courseBegin;
    }

    public Date getCourseEnd() {
        return courseEnd;
    }

    public void setCourseEnd(Date courseEnd) {
        this.courseEnd = courseEnd;
    }

    public void setCourseModificationDate(Date courseModificationDate) {
        this.courseModificationDate = courseModificationDate;
    }

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

    public String getInputSelectCourse() {
        return inputSelectCourse;
    }

    public void setInputSelectCourse(String inputSelectCourse) {
        this.inputSelectCourse = inputSelectCourse;
    }


 @Override
public String toString()
{ return 
        ( NEWLINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEWLINE 
        + " idcourse : "   + this.getIdcourse()
               + " ,course Name : " + this.getCourseName()
               + " ,course Par : " + this.getCoursePar()
               + " ,course Holes : " + this.getCourseHoles()
        );
}
  public static Course mapCourse(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Course c = new Course();
		c.setIdcourse(rs.getInt("idcourse"));
                c.setCourseName(rs.getString("coursename") );
                c.setCourseHoles(rs.getShort("CourseHoles"));
                c.setCoursePar(rs.getShort("coursepar"));
                c.setClub_idclub(rs.getInt("course.club_idclub"));
                c.setCourseBegin(rs.getDate("courseBegin"));
                c.setCourseEnd(rs.getDate("courseEnd"));
        
   return c;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
} // end class
