
package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import utils.LCUtil;

//@Named
@Named("hole")
@RequestScoped // new 21-10-2021
public class Hole implements Serializable, interfaces.Log{
    private static final long serialVersionUID = 1L;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
@NotNull(message="Bean validation : the Hole ID must be completed")
    private Integer idhole;

@NotNull(message="{hole.number.notnull}")
@Min(value=1,message="{hole.number.min}")
@Max(value=18,message="{hole.number.max}")
    private Short holeNumber;

@NotNull(message="{hole.par.notnull}")
    @Min(value=3,message="Bean validation : the Hole Par is Min 3")
    @Max(value=5,message="Bean validation : the Hole Par is Max 5")
    private Short holePar;

@NotNull(message="{hole.distance.notnull}")
@Min(value=59,message="{hole.distance.min}")
@Max(value=540,message="{hole.distance.max}")
    private Short holeDistance;

@NotNull(message="{hole.index.notnull}")
    @Min(value=1,message="Bean validation : the Hole Index is Min 1")
    @Max(value=18,message="Bean validation : the Hole Index is Max 18")
    private Short holeStrokeIndex;

    private Integer tee_idtee;
    private Integer tee_course_idcourse;
    private Date holeModificationDate;
private boolean NextHole; // 15/01/2013
private boolean CreateModify = true; // 12/08/2017

    public Hole() {
        holePar = 4; // default value radio button
        holeNumber = 1;
    }

    public Integer getIdhole() {
        return idhole;
    }

    public void setIdhole(Integer idhole) {
        this.idhole = idhole;
    }

    public Short getHoleNumber() {
        return holeNumber;
    }

    public void setHoleNumber(Short holeNumber) {
        this.holeNumber = holeNumber;
    }

    public Short getHolePar() {
        return holePar;
    }

    public void setHolePar(Short holePar) {
        this.holePar = holePar;
    }

    public Short getHoleDistance()
    {      // LOG.debug("hole distance getter to :" + holeDistance);
        return holeDistance;
    }

    public void setHoleDistance(Short holeDistance)
    {
        this.holeDistance = holeDistance;
         // LOG.debug("hole distance setted to :" + this.holeDistance);
    }

    public Short getHoleStrokeIndex() {
        return holeStrokeIndex;
    }

    public void setHoleStrokeIndex(Short holeStrokeIndex) {
        this.holeStrokeIndex = holeStrokeIndex;
    }

    public Integer getTee_idtee() {
        return tee_idtee;
    }

    public void setTee_idtee(Integer tee_idtee) {
        this.tee_idtee = tee_idtee;
    }

    public Integer getTee_course_idcourse() {
        return tee_course_idcourse;
    }

    public void setTee_course_idcourse(Integer tee_course_idcourse) {
        this.tee_course_idcourse = tee_course_idcourse;
    }

    public Date getHoleModificationDate() {
        return holeModificationDate;
    }

    public void setHoleModificationDate(Date holeModificationDate) {
        this.holeModificationDate = holeModificationDate;
    }

    public boolean isNextHole() {
        return NextHole;
    }

    public void setNextHole(boolean NextHole) {
        this.NextHole = NextHole;
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
//    LOG.debug("starting toString Hole!");
       if(this.getClass() == null){
         return ("Hole is null, no print !");
    }
    return 
        (NEW_LINE + "FROM ENTITE = "+ this.getClass().getSimpleName().toUpperCase()+ NEW_LINE
               + " ,idhole : "   + this.getIdhole()
               + " ,hole number: "   + this.getHoleNumber()
               + " ,Hole Par : " + this.getHolePar()
               + " ,Hole Distance : " + this.getHoleDistance()
               + " ,Hole StrokeIndex : " + this.getHoleStrokeIndex()
               + " ,Hole id tee : " + this.getTee_idtee()
               + " ,Hole id course : " + this.getTee_course_idcourse()
        );
    }catch(Exception e){
        String msg = "£££ Exception in Hole.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}

  public static Hole map(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Hole h = new Hole();
        h.setIdhole(rs.getInt("idhole"));
        h.setHoleNumber(rs.getShort("HoleNumber") );
        h.setHolePar(rs.getShort("HolePar") );
        h.setHoleDistance(rs.getShort("HoleDistance") );
        h.setHoleStrokeIndex(rs.getShort("HoleStrokeIndex") );
        h.setTee_idtee(rs.getInt("tee_idtee"));
        h.setTee_course_idcourse(rs.getInt("tee_course_idcourse"));
        return h;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map


} // end class