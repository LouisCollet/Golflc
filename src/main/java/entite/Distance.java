package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import utils.LCUtil;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
// @JsonInclude(Include.NON_NULL)  // ne fonctionne pas dans table multidimentional intéressant ?
@Named("distance") // nécessaire ??
@RequestScoped

public class Distance implements Serializable{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
//@JsonInclude(Include.NON_NULL)
@JsonIgnore
    private int idTee;
@JsonInclude(Include.NON_NULL)
    private int[] distanceArray;

public Distance(){ // constructor
    } // end constructor

    public int getIdTee() {
        return idTee;
    }

    public void setIdTee(int idTee) {
        this.idTee = idTee;
    }

    public int[] getDistanceArray() {
        return distanceArray;
    }

    public void setDistanceArray(int[] distanceArray) {
        this.distanceArray = distanceArray;
    }

    @PostConstruct
    public void init(){
 //        sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
 //        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
// https://github.com/OWASP/java-html-sanitizer
            LOG.debug("sanitizer started ! = " );
    }

public static Distance map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Distance tm = new Distance();
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
   //     tm = om.readValue(rs.getString("TarifMemberJson"),TarifMember.class);
 //             LOG.debug("TarifMember extracted from database = "  + tm.toString());
   //     tm.setStartDate(rs.getTimestamp("TarifMemberStartDate").toLocalDateTime());
   //     tm.setEndDate(rs.getTimestamp("TarifMemberEndDate").toLocalDateTime());
    //    tm.setTarifMemberIdClub(rs.getInt("TarifMemberIdClub"));
      LOG.debug("Distance tm returned from map = " + tm);
   return tm;
}catch(Exception e){
   String msg = "£££ Exception in " + methodName + " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
 @Override
public String toString(){
 try {
    LOG.debug("starting toString Distance !");
    return
            (NEW_LINE  + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + NEW_LINE + "<br>"
             + "idtee : " + idTee //.format(ZDF_TIME_DAY)
            + NEW_LINE + "<br>"
            + "DistanceArray : "   + Arrays.toString(distanceArray)
         //   + "finito"
            );
     } catch (Exception ex) {
           LOG.error("Exception in Distance to String" + ex);
           return null;
        }
} //end method
} // end class