package create;

import entite.Creditcard;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;

// encore à faire !! dans le futur, l'appeler à partir de create player n'est pas utilisé !!!
// called from FindSubscriptionStatus
public class CreateCreditcard implements interfaces.Log, interfaces.GolfInterface{
    public boolean create(Creditcard creditcard,final Connection conn) throws SQLException{
        PreparedStatement ps = null;
        try {
            LOG.debug("...entering createCreditcard");
            LOG.debug("player  = " + creditcard.getCreditCardIdPlayer());
            LOG.debug("creditcard  = " + creditcard.toString() );

            final String query = LCUtil.generateInsertQuery(conn, "creditcard"); 
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setInt(2, creditcard.getCreditCardIdPlayer());
            ps.setString(3,creditcard.getCreditcardHolder());
            ps.setString(4,creditcard.getCreditcardNumber());
            ps.setTimestamp(5, Timestamp.valueOf(creditcard.getCreditCardExpirationDateLdt()));
            ps.setString(6,creditcard.getCreditcardType());
            ps.setShort(7, creditcard.getCreditcardVerificationCode());
            ps.setTimestamp(8, Timestamp.from(Instant.now()));
 
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
            if(row != 0){
                String msg = "Creditcard created for player = " + creditcard.getCreditCardIdPlayer(); //getIdplayer();
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            }else{
                String msg = "<br/><br/>ERROR insert for creditcard : "  + creditcard.getCreditCardIdPlayer(); //getIdplayer();
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
        }catch (SQLException sqle){
            //LOG.error("-- SQLException in Insert Course " + sqle.toString());
            String msg = "SQLException in createCreditcard = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }catch(Exception e){
            String msg = "£££ Exception in createCreditcard = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
    } //end method
    
 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try{
  //          Player player = new Player();
    //        player.setIdplayer(324713);
    }catch (Exception e){
            String msg = "££ Exception in main CreateBlocking = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//

} //end Class