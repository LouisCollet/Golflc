package create;

import entite.Creditcard;
import entite.Player;
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
    public boolean create(final Player player, Creditcard creditcard,final Connection conn) throws SQLException{
        PreparedStatement ps = null;
        try {
            LOG.info("...entering createCreditcard");
            LOG.info("player  = " + player.getIdplayer());
            LOG.info("creditcard  = " + creditcard.toString() );

            final String query = LCUtil.generateInsertQuery(conn, "creditcard"); 
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setInt(2, player.getIdplayer());
      //          LOG.info("LocalDate now() = " + LocalDate.now());
       //     java.sql.Timestamp ts = Timestamp.valueOf(LocalDate.now().minusDays(1).atStartOfDay());  // day before today
       //         LOG.info("new startdate and endDate inserted in DB = " + ts);
      //          LocalDate localDate = LocalDate.of(2016,8,19);    alternative fixed date
       //     ps.setTimestamp(3, ts);  // start subscription date
       //     ps.setTimestamp(4, ts);  // end subscription date
            ps.setString(3,creditcard.getCreditCardHolder());
            ps.setString(4,creditcard.getCreditCardNumberNonSecret());  // sans les ****
            ps.setDate(5, LCUtil.getSqlDate(creditcard.getCreditCardExpirationDate()));
            ps.setString(6,creditcard.getCreditCardType());
            ps.setTimestamp(7, Timestamp.from(Instant.now()));
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
            if(row != 0){
                String msg = "Creditcard created for player = " + player.getIdplayer();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            }else{
                String msg = "<br/><br/>ERROR insert for creditcard : "  + player.getIdplayer();
                LOG.info(msg);
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
} //end Class