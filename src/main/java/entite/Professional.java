package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
// import jakarta.enterprise.context.RequestScoped;  // migrated 2026-02-24
// import jakarta.inject.Named;  // migrated 2026-02-24
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import utils.LCUtil;

// @Named("pro")  // migrated 2026-02-24
// @RequestScoped  // migrated 2026-02-24

public class Professional implements Serializable{
    

@NotNull(message="{unavailable.startdate.notnull}")
    private LocalDateTime proStartDate;
@NotNull(message="{unavailable.enddate.notnull}")
    private LocalDateTime proEndDate;

@NotNull(message="{player.id.notnull}")
    private Integer proPlayerId;

//@NotNull(message="Bean validation : the Club ID must be completed")
//  private Integer idclub;

@NotNull(message="{professional.amount.notnull}")
    private Double proAmount;
private Integer proId;
private Integer proClubId;
public Professional(){ // constructor
 //  LOG.debug("constructor Professional executed !");
    } // end constructor

 //   @PostConstruct
//    public void init(){
 //       LOG.debug("init pro");
 //   }

    public LocalDateTime getProStartDate() {
        return proStartDate;
    }

    public void setProStartDate(LocalDateTime proStartDate) {
        this.proStartDate = proStartDate;
    }

    public LocalDateTime getProEndDate() {
        return proEndDate;
    }

    public void setProEndDate(LocalDateTime proEndDate) {
        this.proEndDate = proEndDate;
    }

    public Integer getProId() {
        return proId;
    }

    public void setProId(Integer proId) {
        this.proId = proId;
    }

    public Integer getProPlayerId() {
        return proPlayerId;
    }

    public void setProPlayerId(Integer proPlayerId) {
        this.proPlayerId = proPlayerId;
    }

    public Integer getProClubId() {
        return proClubId;
    }

    public void setProClubId(Integer proClubId) {
        this.proClubId = proClubId;
    }

    public Double getProAmount() {
        return proAmount;
    }

    public void setProAmount(Double proAmount) {
        this.proAmount = proAmount;
    }
/* migré 25-01-2026
    public static Professional map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try{
            Professional pro = new Professional();
            // besoin proid ?
            pro.setProId(rs.getInt("ProId"));
            pro.setProClubId(rs.getInt("ProClubId"));
            pro.setProStartDate(rs.getTimestamp("ProClubStartDate").toLocalDateTime());
            pro.setProEndDate(rs.getTimestamp("ProClubEndDate").toLocalDateTime());
            pro.setProPlayerId(rs.getInt("ProPlayerId"));
            pro.setProAmount(rs.getDouble("ProAmount")); // new 06-06-2021
    //           LOG.debug("Professional event returned from map = " + pro);
            return pro;
        }catch(Exception e){
            String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } } //end method
    
    */
 @Override
public String toString(){
 try {
    LOG.debug("starting toString Professional !");
    return
            (NEW_LINE  + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + NEW_LINE + "<br>"  + "Pro Start Date : "   + this.proStartDate //.format(ZDF_TIME)
            + NEW_LINE + "<br>"  + "Pro End Date : "   + this.proEndDate //.format(ZDF_TIME)
            + NEW_LINE + "<br>"  + "Pro Id: "   + this.proId
            + NEW_LINE + "<br>"  + "Pro Id player : " + this.proPlayerId
            + NEW_LINE + "<br>"  + "Pro Id club : " + this.proClubId
            + NEW_LINE + "<br>"  + "Pro prix lesson : " + this.proAmount
            );
        } catch (Exception ex) {
           LOG.error("Exception in Professional to String" + ex);
           return null;
        }
} //end method
} // end class