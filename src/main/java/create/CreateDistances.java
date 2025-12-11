package create;

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
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class CreateDistances {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
// public boolean create(final CompetitionDescription competition, final Connection conn) throws SQLException{
  public boolean create(final Distance distance, final Connection conn) throws SQLException, LCException{   
     
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        LOG.debug("... entering ... " + methodName); 
        LOG.debug("with distance = " + distance);
    PreparedStatement ps = null;
    
try{
    if(distance.getDistanceArray() == null){
        LOG.debug("distancearray is null - skipped");
        return false;
    }
    final String query = LCUtil.generateInsertQuery(conn, "distances"); 
    ps = conn.prepareStatement(query);
   // updated fields
    ps.setInt(1, distance.getIdTee()); // 10 = test = idtee
  //  ObjectMapper om = new ObjectMapper();
     //   String json = om.writeValueAsString(distance); // sur class et pas sur field attention ici erreur cherché longtemps !!
        String json = new ObjectMapper().writeValueAsString(distance); 
       LOG.debug("distances converted in json format = " + NEW_LINE + json);
    ps.setString(2, json);
    ps.setTimestamp(3, Timestamp.from(Instant.now()));
    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate(); // write into database
   
    if(row!=0){
        String msg =  LCUtil.prepareMessageBean("distance.create") + distance;
        var v = distance.getDistanceArray();
        Arrays.stream(v).forEach(e->LOG.debug(e + ","));
        msg = msg + "<br>Vérification : total = " + Arrays.stream(v).sum();
                //https://docs.oracle.com/javase/8/docs/api/java/util/Arrays.html#stream-int:A-int-int-
        msg = msg + " ,out = " + Arrays.stream(v,0,9).sum();
        msg = msg + " ,in = " + Arrays.stream(v,9,18).sum();
        LOG.debug(msg); 
        showMessageInfo(msg);
        return true;
     }else{
        String msg = "-- ERROR update Distances : " + distance; 
        LOG.debug(msg); 
        showMessageFatal(msg);
        return false;
     }
//  } // end for
  
//return true;
} catch(SQLException sqle) {
    // ici tester sur l'erreur si laredy exists ==> on fait l'update !!!
    //https://dev.mysql.com/doc/connector-j/8.1/en/connector-j-reference-error-sqlstates.html
        if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 ){  // duplicate entry les 2 tests sont synonymes le code 1062 est vendor-specific à mysql = ER_DUP_ENTRY
            LOG.info("distances already exists - going to update");
             //    msg = LCUtil.prepareMessageBean("create.player.fail") + player.getIdplayer();
            boolean b = new update.UpdateDistances().update(distance, conn);
            LOG.debug("back from update = " + b);
          //       showMessageFatal(msg);
            return true;
        }else{
            utils.LCUtil.printSQLException(sqle);
            String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
        }
 //    } catch(LCException e) {
 //      LOG.error(" -- LC Exception in " + methodName + e.getMessage());
 //      return false;   
    } catch(Exception e) {
       LOG.error(" -- Exception in " + methodName + e.getMessage());
       return false;
    }finally{
        DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
    }
} //end create
 

 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
 try{
    Distance distance = new Distance();
    distance.setIdTee(218);
    int ar[] = {335,511,140,333,273,442,318,171,407,355,307,180,398,365,472,138,337,399};
    distance.setDistanceArray(ar);
        LOG.debug("array to insert json = " + Arrays.toString(distance.getDistanceArray()));
    boolean lp = new CreateDistances().create(distance, conn);
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