package sql.preparedstatement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;


public class psCreateUpdateClub implements Serializable, interfaces.Log, interfaces.GolfInterface{
    
 public static PreparedStatement psMapUpdate(PreparedStatement ps, Club club) throws Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
      // voir aussi http://www.javased.com/index.php?source_dir=archaius/archaius-core/src/main/java/com/netflix/config/sources/JDBCConfigurationSource.java
      int index = 0;
      // mod 16-12-2025
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
    //    om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
       // String json = om.writeValueAsString(club.getUnavailableStructure()); //. prend 3 fields ??
        String json = club.getUnavailableStructure() != null
              ? om.writeValueAsString(club.getUnavailableStructure())
              : "{}";                   // ✅ JSON vide par défaut
           LOG.debug("UnavailableStructure data converted in json format = " + NEW_LINE + json);
            ps.setString(++index, club.getClubName());   // 1
            ps.setString(2, club.getAddress().getStreet());
            ps.setString(3, club.getAddress().getCity());
            ps.setString(4, club.getAddress().getCountry().getCode().toUpperCase());
            ps.setDouble(5, club.getAddress().getLatLng().getLat()); 
            ps.setDouble(6, club.getAddress().getLatLng().getLng());
            ps.setString(7, club.getClubWebsite());
            ps.setString(8, club.getAddress().getZoneId());
            ps.setInt(9, club.getClubLocalAdmin());
            ps.setString(10, json); // new 19-12-2025
            ps.setInt(11, club.getIdclub());  // clé de recherche
            ps.getWarnings(); // new 27-04-2025
   //// ps. 12 modificationDate non nécessaire (faite par DB System)
return ps;
  }catch(Exception e){
     handleGenericException(e, methodName);
     return null;
  }
} //end method
 
 public static PreparedStatement psMapCreate(PreparedStatement ps, Club club){
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
      
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    String json = club.getUnavailableStructure() != null
              ? om.writeValueAsString(club.getUnavailableStructure())
              : "{}";                   // ✅ JSON vide par défaut
      int index = 0;
            ps.setNull(++index, java.sql.Types.INTEGER);
            ps.setString(2, club.getClubName());
            ps.setString(3, club.getAddress().getStreet());
            ps.setString(4, club.getAddress().getZipCode() + club.getAddress().getCity()); // zipcode ajouté 19-12-2025
              LOG.debug("Club country too long ? = " + club.getAddress().getCountry().getCode().toUpperCase());
            ps.setString(5, club.getAddress().getCountry().getCode().toUpperCase()); // chipotage transitoire !!
            ps.setDouble(6, club.getAddress().getLatLng().getLat()); 
            ps.setDouble(7, club.getAddress().getLatLng().getLng());
            ps.setString(8, club.getClubWebsite());
            if(club.getAddress().getZoneId() != null){
                 ps.setString(9, club.getAddress().getZoneId());
            }else{
                ps.setString(9, "Europe/Brussels");
            }
            ps.setInt(10,324713);  // mod 29-03-2019 default LocalAdmin
            // ps.setString(11,null);// json unavailableStructure
            ps.setString(11, json); // mod par claude 20-02-2026 mod 18-12-2025 sur suggestion chatgpt
            
            ps.setTimestamp(12, Timestamp.from(Instant.now()));
            ps.getWarnings(); // new 27-04-2025
return ps;
  }catch(Exception e){
   String msg = "£££ Exception in psClubCreate = " + methodName + " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} //end class