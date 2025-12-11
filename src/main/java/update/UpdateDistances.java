package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.Distance;
import exceptions.LCException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.IntStream;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateDistances{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 public boolean update(Distance distance, Connection conn) throws SQLException, LCException{
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug("entering " + methodName); 
        LOG.debug(" with distances = " + distance);
 //       LOG.debug("tee = " + tee.toString());
    PreparedStatement ps = null;
try{
    String distances = utils.DBMeta.listMetaColumnsUpdate(conn, "distances"); // pas MAJ blacklist !!
     // %s indique qu'il s'agit d'un string dans est le même pour toutes les query
    final String query = """
            UPDATE distances
            SET %s
            WHERE DistanceIdTee = ?
           """.formatted(distances);

     ps = conn.prepareStatement(query);
     String json = new ObjectMapper().writeValueAsString(distance); // sur class et pas sur field attention ici erreur cherché longtemps !!
           LOG.debug("distances converted in json format = " + NEW_LINE + json);
     ps.setString(1, json);
     ps.setTimestamp(2, Timestamp.from(Instant.now()));  // !! blacklist pas MAJ avec DistanceModificationDate car pas possible d'activer CURRENT_TIMESTAMP avec mySQL 8.1
     ps.setInt(3, distance.getIdTee());
     utils.LCUtil.logps(ps);
     int row = ps.executeUpdate(); // write into database
     if(row!=0){
                LOG.debug("-- Successfull updateDistances : " + distance);
                var v = distance.getDistanceArray();
                Arrays.stream(v).forEach(e->LOG.debug(e + ","));
                String msg =  LCUtil.prepareMessageBean("distance.update") + distance;
                msg = msg + "<br>Vérification : total = " + Arrays.stream(v).sum();
                //https://docs.oracle.com/javase/8/docs/api/java/util/Arrays.html#stream-int:A-int-int-
                msg = msg + " ,out = " + Arrays.stream(v,0,9).sum();
                msg = msg + " ,in = " + Arrays.stream(v,9,18).sum();
                LOG.debug(msg); 
                LCUtil.showMessageInfo(msg);
                return true;
     }else{
                String msg = "-- FATAL ERROR updateDistances : " + distance; 
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                Exception e = new Exception(msg); 
                throw new LCException("LCException in : " + methodName, e);   // à traiter dans calling program !!
                //return false;
            }
  //   } // end for

//return true;
} catch(SQLException sqle) {
       String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
       return false;
} catch(LCException e) {
       LOG.error(" -- LC Exception in  " + methodName + e.getMessage());
       throw e;
     //  return false;       
} catch(Exception e) {
       LOG.error(" -- Exception in  " +methodName + e.getMessage());
       return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
      //  return false;
    }
} //end updateDistances
 
  void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
 try{
    Distance distance = new Distance();
    distance.setIdTee(2);
    int ar[] = {200,210,220,333,273,442,318,171,407,355,307,180,398,365,472,138,337,399};
    distance.setDistanceArray(ar);
        LOG.debug("array to insert json = " + Arrays.toString(distance.getDistanceArray()));
    boolean lp = new UpdateDistances().update(distance, conn);
        LOG.debug("from main, after lp = " + lp);
    }catch (Exception e){
            String msg = "££ Exception in main CreateDistance = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//
 
 
 
} // end class