
package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.inject.Named;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import utils.LCUtil;


@Named
public class Tee implements Serializable, interfaces.Log{
    private static final long serialVersionUID = 1L;

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
@Min(value=90,message="{tee.slope.min}")
@Max(value=138,message="{tee.slope.max}")
    private Short teeSlope;

@NotNull(message="{tee.rating.notnull}")
@DecimalMin(value="58.0",message="{tee.rating.min}")
@DecimalMax(value="75.0",message="{tee.rating.max}")
private BigDecimal teeRating;

@NotNull(message="{tee.clubhandicap.notnull}")
@Min(value=00,message="{tee.clubhandicap.min}")
@Max(value=10,message="{tee.clubhandicap.max}")
    private Integer teeClubHandicap; // was Short

private Integer course_idcourse;

private Date teeModificationDate;
private boolean NextTee; // 23/06/2013
private boolean CreateModify = true; // 12/08/2017

@NotNull(message="Bean validation : the TeeStart must be completed")
// @Size(max = 5,message="Bean validation : the Hcp is maximum 5 characters")
    private String teeHolesPlayed;

    public Tee(){ // connector
        teeGender="M"; // default for radio button
        teeStart="YELLOW";
        teeClubHandicap = 0;
    }

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
 //       LOG.info("teeStart setted = " + teeStart);
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

    public Date getTeeModificationDate() {
        return teeModificationDate;
    }

    public void setTeeModificationDate(Date teeModificationDate) {
        this.teeModificationDate = teeModificationDate;
    }

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

    public String getTeeHolesPlayed() {
        return teeHolesPlayed;
    }

    public void setTeeHolesPlayed(String teeHolesPlayed) {
        this.teeHolesPlayed = teeHolesPlayed;
    }
    
@Override
public String toString(){
    try{
        LOG.info("starting toString Tee!");
    return 
        ( NEW_LINE  + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE
               + " idtee : "   + this.getIdtee()
               + " ,TeeStart : " + this.getTeeStart()
               + " ,Tee Slope : " + this.getTeeSlope()
               + " ,Tee Rating : " + this.getTeeRating()
               + " ,Tee Gender : " + this.getTeeGender()
               + " ,idcourse : " + this.getCourse_idcourse()
               + " ,holes played : " + this.getTeeHolesPlayed()
        );
        }catch(Exception e){
        String msg = "£££ Exception in Tee.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}   

  public static Tee mapTee(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Tee t = new Tee();
        t.setIdtee(rs.getInt("idtee"));
        t.setTeeGender(rs.getString("TeeGender"));
        t.setTeeStart(rs.getString("TeeStart"));
        t.setTeeSlope(rs.getShort("teeslope"));
        t.setTeeRating(rs.getBigDecimal("teerating"));
        t.setTeeClubHandicap(rs.getInt("TeeClubHandicap"));
        t.setCourse_idcourse(rs.getInt("tee.course_idcourse"));
        t.setTeeHolesPlayed(rs.getString("TeeHolesPlayed")); // new 29-03-2019
                ;
        
   return t;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map

} // end class