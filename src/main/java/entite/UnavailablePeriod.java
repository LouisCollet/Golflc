package entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import static interfaces.Log.LOG;
// import jakarta.enterprise.context.RequestScoped;  // migrated 2026-02-24
// import jakarta.inject.Named;  // migrated 2026-02-24
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

// @Named  // migrated 2026-02-24
// @RequestScoped  // migrated 2026-02-24
//@JsonPropertyOrder({"startDate", "endDate", "itemPeriod", "nowDate"})
@JsonInclude(JsonInclude.Include.NON_NULL) //  ignore all null fields
public class UnavailablePeriod implements Serializable, interfaces.Log, interfaces.GolfInterface{
 
 @JsonIgnore private static final long serialVersionUID = 1L;
 
 //  going to json 
 
  private Boolean[] itemPeriod;
  
  //private ArrayList<String> itemListPeriod = new ArrayList<>();
  //private ArrayList<Boolean> itemListPeriod = new ArrayList<>();
  private String comment;
  
 // not in database json field
 @JsonIgnore    private Integer idclub;
 //   @FutureOrPresent(message="Start date future or present !")
    @NotNull(message="{unavailable.startdate.notnull}")
 @JsonIgnore private LocalDateTime startDate;
    
 //   @FutureOrPresent(message="End date future or present !")  // default message
    @NotNull(message="{unavailable.enddate.notnull}")
 @JsonIgnore private LocalDateTime endDate;

public UnavailablePeriod(){    // constructor
 // int i = new UnavailableStructure().getItemStructure().length; fonctionne mais overhead !
 // on pourrai en faire un paramètre dans Settings !!
    //LOG.debug("longueur dérivée de Structure = " + i);  pour lier les deux !!
 itemPeriod = new Boolean[20]; 
 // même longueur que itemStructure de UnavailableStucture
}

    public Integer getIdclub() {
        return idclub;
    }

    public void setIdclub(Integer idclub) {
        this.idclub = idclub;
    }

    public Boolean[] getItemPeriod() {
        return itemPeriod;
    }

    public void setItemPeriod(Boolean[] itemPeriod) {
        this.itemPeriod = itemPeriod;
    }



    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

 public void setStartDate(LocalDateTime startDate) {
    //    LOG.debug("setStartDate ldt = " + startDate);
        this.startDate = startDate;
    }

public String getComment(){
    return  comment;
}

    public void setComment(String comment) {
        this.comment = comment;
    }



 @Override
   public String toString() {
   try { 
       return
                (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName().toUpperCase()
                + "<br/>idclub : "   + this.getIdclub()
                + " ,startDate : "  + this.getStartDate()
                + " ,endDate : "    + this.getEndDate()
                + " ,array itemPeriod (boolean): "   + Arrays.deepToString(utils.LCUtil.removeNull1DBoolean(getItemPeriod()))
//               + " ,array Period: "   +  Arrays.deepToString(getItemPeriod())
 //               + " ,itemListPeriod: "   + itemListPeriod.toString()
                + " ,comment : "    + this.getComment()
                );
  }catch(Exception e){
    String msg = "£££ Exception in UnavailablePeriod.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
  } //end method
/* vers row mapper        
public static UnavailablePeriod map(ResultSet rs) throws SQLException{
  final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
        ObjectMapper om = new ObjectMapper();
        UnavailablePeriod period = om.readValue(rs.getString("UnavailableItems"), UnavailablePeriod.class);
        period.setIdclub(rs.getInt("UnavailableIdClub"));
        period.setStartDate(rs.getTimestamp("UnavailableStartDate").toLocalDateTime());
   //        LOG.debug("start date column = " + rs.getTimestamp("UnavailableStartDate").toLocalDateTime());
        period.setEndDate(rs.getTimestamp("UnavailableEndDate").toLocalDateTime());
   //        LOG.debug("end date column = " + rs.getTimestamp("UnavailableEndDate").toLocalDateTime());
   return period;
  }catch(Exception e){
      String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
  }
} //end method
*/
// new 26-03-2020 experimental !!
public static PreparedStatement psUnavailablePeriodCreate(PreparedStatement ps, Club club){
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
      // voir aussi http://www.javased.com/index.php?source_dir=archaius/archaius-core/src/main/java/com/netflix/config/sources/JDBCConfigurationSource.java
      int index = 0;
            ps.setString(++index, club.getClubName());   // 1
            ps.setString(2, club.getAddress().getStreet());
     //       ps.setString(3, club.getClubCity());
            ps.setString(3, club.getAddress().getCity());
        //    ps.setString(4, club.getAddress().getCountry());
            // mod 22-12-2022
            ps.setString(4, club.getAddress().getCountry().getCode());
            ps.setDouble(5, club.getAddress().getLatLng().getLat());
            ps.setDouble(6, club.getAddress().getLatLng().getLng());
          //  ps.setDouble(5, club.getClubLatitude());
          //  ps.setDouble(6, club.getClubLongitude());
            ps.setString(7, club.getClubWebsite());
            ps.setString(8, club.getAddress().getZoneId() ); // new 01/08/2017 using GoogleTimeZone
            ps.setInt(9, club.getClubLocalAdmin());
    //        ps.setString(10, json); fait localement car donnée locale
   //// ps. 12 modification date non nécessaire (faite par mySQL)
return ps;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class