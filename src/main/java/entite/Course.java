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
@Size(min=1,max=2,message="Bean validation : the Round Game is min 3, max 20 characters")
@Min(value=9,message="{tee.slope.min}")
@Max(value=18,message="{tee.slope.max}")
    private short courseHoles;

@NotNull(message="{course.par.notnull}")
@Min(value=69,message="{tee.slope.min}")
@Max(value=73,message="{tee.slope.max}")
    private Short coursePar;

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
   //      LOG.info("getCoursepar = " + coursePar);
        return coursePar;
    }

    public void setCoursePar(Short coursePar) {
        this.coursePar = coursePar;
 //       LOG.info("setCoursepar to " + this.coursePar);
    }

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
   //     LOG.info("getInputSelectCourse = " + inputSelectCourse);
        return inputSelectCourse;
    }

    public void setInputSelectCourse(String inputSelectCourse) {
        this.inputSelectCourse = inputSelectCourse;
        LOG.info("setInputSelectCourse = " + inputSelectCourse);
    }

 @Override
public String toString()
{ return 
        ( NEWLINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEWLINE 
        + " idcourse : "   + this.getIdcourse()
               + " ,course Name : " + this.getCourseName()
               + " ,course Par : " + this.getCoursePar()
               + " ,course Holes : " + this.getCourseHoles()
               + " ,Begin course = " + this.getCourseBegin()
               + " ,End course = " + this.getCourseEnd()
               + " ,inputSelectCourse = " + this.getInputSelectCourse()
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
                c.setCourseBegin(rs.getTimestamp("courseBegin")); // format 'DATE' in database
                c.setCourseEnd(rs.getTimestamp("courseend")); // format 'DATE' in database
                
   return c;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
} // end class