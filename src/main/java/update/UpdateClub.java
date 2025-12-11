package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import read.ReadClub;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateClub implements Serializable, interfaces.Log, interfaces.GolfInterface{
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 // à vérifier !!
public boolean update(final Club club, final Connection conn) throws Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        PreparedStatement ps = null;
    try {
            LOG.debug("Entering " + methodName);
            LOG.debug(" with club = "+ club);
   // new 22-03-2020
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());  // new 18/01/2019 traiter LocalDateTime format 
        om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
        String json = om.writeValueAsString(club.getUnavailableStructure()); //. prend 3 fields ??
           LOG.debug("UnavailableStructure data converted in json format = " + NEW_LINE + json);
        String cl = utils.DBMeta.listMetaColumnsUpdate(conn, "club");
            LOG.debug("String from listMetaColumns = " + cl);
    //    String query = 
    //        "UPDATE club"
    //        + " SET " + cl
    //        + " WHERE club.idclub=?"
    //        ;
        final String query = """
          UPDATE club
          SET %s
          WHERE club.idclub=?;
         """.formatted(cl) ;
        
            ps = conn.prepareStatement(query);
            ps = Club.psClubUpdate(ps,club);  // special club !
            ps.setString(10, json); // car donnée locale
   //// clé de la recherche !!!
            ps.setInt(11, club.getIdclub());  // ne pas oublier
            
            utils.LCUtil.logps(ps);
            int rowsAffected = ps.executeUpdate(); // write into database
                LOG.debug("row = " + rowsAffected);
            if(rowsAffected != 0){
                String msg =  LCUtil.prepareMessageBean("club.update") + club;
         //       msg = msg // + "<h1> successful modify Player : "
        //                    + " <br/>ID = " + club.getIdclub()
        //                    + " <br/>Name = " + club.getClubName()
         //                   + " <br/>Address = " + club.getAddress().getStreet();
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "row = 0 - Could not update club";
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw (new SQLException(msg));
                //    return false; pas compatible avec throw
            }
return true;
  }catch(SQLException sqle) {
            String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyClub

         void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
     Club club = new Club();
     club.setIdclub(1104);
     Club c = new ReadClub().read(club, conn);
     c.setClubName(club.getIdclub() + "modified");
     boolean b = new UpdateClub().update(c,conn);
         LOG.debug("from main, resultat = " + b);
 }catch (Exception e){
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//



} //end Class