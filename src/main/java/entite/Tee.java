
package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import utils.LCUtil;

@Named
@RequestScoped
public class Tee implements Serializable, interfaces.Log{
    private static final long serialVersionUID = 1L;
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
//@NotNull(message="Bean validation : the Tee ID must be completed")
    private Integer idtee;

@NotNull(message="Bean validation : the Gender must be completed")
    @Size(max = 1,message="Bean validation : the Gender is maximum 1 character")
    private String teeGender;

@NotNull(message="Bean validation : the TeeStart must be completed")
// @Size(max = 5,message="Bean validation : the Hcp is maximum 5 characters")
    private String teeStart;

@NotNull(message="{tee.slope.notnull}")
//@Size(min=90, max=138, message="{tee.slope.minmax}")
@Min(value=88,message="{tee.slope.min}")
@Max(value=152,message="{tee.slope.max}")
    private Short teeSlope;

@NotNull(message="{tee.rating.notnull}")
@DecimalMin(value="28.0",message="{tee.rating.min}")
@DecimalMax(value="77.4",message="{tee.rating.max}")
private BigDecimal teeRating;

@NotNull(message="{tee.clubhandicap.notnull}")
@Min(value=00,message="{tee.clubhandicap.min}")
@Max(value=10,message="{tee.clubhandicap.max}")
    private Integer teeClubHandicap; // was Short
@NotNull(message="{tee.par.notnull}")
    private Short teePar;
private boolean notFound;

private Integer course_idcourse;

// private Date teeModificationDate;
private boolean NextTee; // 23/06/2013
private boolean CreateModify = true; // 12/08/2017
private boolean ModifyClubCourseTee = true; // 11-08-2023
private Integer teeMasterTee;
private Integer teeDistanceTee;
@NotNull(message="Bean validation : the TeeStart must be completed")
// @Size(max = 5,message="Bean validation : the Hcp is maximum 5 characters")
    private String teeHolesPlayed;
  
    public Tee(){ // connector
        teeGender="M"; // default for radio button
        teeStart="YELLOW";
        teeClubHandicap = 0;
        teeHolesPlayed = "01-18";
    }
    public enum StartType {YELLOW,WHITE,BLACK,BLUE,RED,ORANGE}
    public StartType[] StartType() { // new 25-08-2023
        return StartType.values();
    }
    
    List<String> parList = Arrays.asList("73","72","71","70","69","62","36","35","34");

    public List<String> getParList() {
        return parList;
    }

 //   public void setParList(List<String> parList) {
 //       this.parList = parList;
 //   }
   
    
    public Integer getIdtee() {
        return idtee;
    }

    public void setIdtee(Integer idtee) {
        this.idtee = idtee;
    }

    public String getTeeGender() {
        return teeGender;
    }

    public void setTeeGender(String teeGender) {
        
        this.teeGender = teeGender;
    }

    public String getTeeStart() {
        return teeStart;
    }

    public void setTeeStart(String teeStart) {
 //       LOG.debug("teeStart setted = " + teeStart);
        this.teeStart = teeStart;
    }

    public Short getTeeSlope() {
        return teeSlope;
    }

    public void setTeeSlope(Short teeSlope) {
        this.teeSlope = teeSlope;
    }

    public BigDecimal getTeeRating() {
        return teeRating;
    }

    public void setTeeRating(BigDecimal teeRating) {
        this.teeRating = teeRating;
    }

    public Integer getTeeClubHandicap() {
        return teeClubHandicap;
    }

    public void setTeeClubHandicap(Integer teeClubHandicap) {
        this.teeClubHandicap = teeClubHandicap;
    }

    public Integer getCourse_idcourse() {
        return course_idcourse;
    }

    public void setCourse_idcourse(Integer course_idcourse) {
        this.course_idcourse = course_idcourse;
    }

 //   public Date getTeeModificationDate() {
//        return teeModificationDate;
//    }

//    public void setTeeModificationDate(Date teeModificationDate) {
//        this.teeModificationDate = teeModificationDate;
//    }

    public boolean isNextTee() {
        return NextTee;
    }

    public void setNextTee(boolean NextTee) {
        this.NextTee = NextTee;
    }

    public boolean isCreateModify() {
        return CreateModify;
    }

    public void setCreateModify(boolean CreateModify) {
        this.CreateModify = CreateModify;
    }

    public boolean isModifyClubCourseTee() {
        return ModifyClubCourseTee;
    }

    public void setModifyClubCourseTee(boolean ModifyClubCourseTee) {
        this.ModifyClubCourseTee = ModifyClubCourseTee;
    }

    public String getTeeHolesPlayed() {
        return teeHolesPlayed;
    }

    public void setTeeHolesPlayed(String teeHolesPlayed) {
        this.teeHolesPlayed = teeHolesPlayed;
    }

    public Short getTeePar() {
        return teePar;
    }

    public void setTeePar(Short teePar) {
        this.teePar = teePar;
    }

    public Integer getTeeMasterTee() {
        return teeMasterTee;
    }

    public void setTeeMasterTee(Integer teeMasterTee) {
        this.teeMasterTee = teeMasterTee;
    }

    public Integer getTeeDistanceTee() {
        return teeDistanceTee;
    }

    public void setTeeDistanceTee(Integer teeDistanceTee) {
        this.teeDistanceTee = teeDistanceTee;
    }

    public boolean isNotFound() {
        return notFound;
    }

    public void setNotFound(boolean notFound) {
        this.notFound = notFound;
    }

    
@Override
public String toString(){
    try{
 //       LOG.debug("starting toString Tee!");
    return 
        ( NEW_LINE  + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE
               + " idtee : "   + this.getIdtee()
               + " ,TeeStart : " + this.getTeeStart()
               + " ,Tee Slope : " + this.getTeeSlope()
               + " ,Tee Rating : " + this.getTeeRating()
               + " ,Tee Gender : " + this.getTeeGender()
               + " ,idcourse : " + this.getCourse_idcourse()
               + " ,holes played : " + this.getTeeHolesPlayed()
               + " ,tee par : " + this.getTeePar()
           //    + " ,tee id_course : " + this.course_idcourse
               + " ,tee Master Tee : " + this.getTeeMasterTee()
               + " ,tee Distance Tee : " + this.getTeeDistanceTee()
               + " ,tee NotFound : " + this.isNotFound()
        );
 }catch(Exception e){
        String msg = "£££ Exception in Tee.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}   

  public static Tee dtoMapper(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Tee t = new Tee();
        t.setIdtee(rs.getInt("idtee"));
        t.setTeeGender(rs.getString("TeeGender"));
        t.setTeeStart(rs.getString("TeeStart"));
        t.setTeeSlope(rs.getShort("teeslope"));
        t.setTeeRating(rs.getBigDecimal("teerating"));
        t.setTeeClubHandicap(rs.getInt("TeeClubHandicap"));  // sert à quoi ??
        t.setCourse_idcourse(rs.getInt("tee.course_idcourse"));
        t.setTeeHolesPlayed(rs.getString("TeeHolesPlayed"));
        t.setTeePar(rs.getShort("TeePar"));
        t.setTeeMasterTee(rs.getInt("TeeMasterTee"));
        t.setTeeDistanceTee(rs.getInt("TeeDistanceTee"));
    return t;
  }catch(Exception e){
     String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage();
     LOG.error(msg);
     LCUtil.showMessageFatal(msg);
     return null;
  }
} //end method map

} // end class