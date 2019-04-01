package modify;

import entite.Creditcard;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyCreditcard{
public boolean modify(final Player player, Creditcard creditcard , final Connection conn) throws SQLException {
   PreparedStatement ps = null;
  try {
            LOG.info("starting modifyCreditcard");
            LOG.info("with Creditcard =  " + creditcard.toString());
     
            final String query // à modifier
              = "  UPDATE creditcard" +
            "      SET CreditcardHolder = ? ," +
            "          CreditcardNumber = ?," +
            "          CreditcardExpirationDate = ?," +
            "          CreditcardType = ?" +
            "      WHERE" +
            "          CreditcardIdPlayer=?";
            ps = conn.prepareStatement(query);
            ps.setString(1,creditcard.getCreditCardHolder());
            ps.setString(2,creditcard.getCreditCardNumberNonSecret());  // sans les ****
            ps.setDate(3,LCUtil.getSqlDate(creditcard.getCreditCardExpirationDate()));
            ps.setString(4,creditcard.getCreditCardType());
            ps.setInt(5,player.getIdplayer());
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate();
                LOG.info("rows = " + row);
            if (row != 0) {
                  LOG.info("before creditcard success msg");
                 String msg =  LCUtil.prepareMessageBean("creditcard.success") + creditcard.getCreditCardHolder()
                //         + " , new end date = " + d.format(ZDF_DAY) + "</h1>"
                           ;
                    LOG.info(msg);
                 LCUtil.showMessageInfo(msg);
                    return true;
             }else{
                   String msg = "NOT NOT Successful update, row = 0 "
    //                            + " hole  = " + (i + 1)
                           + " player = " + creditcard.getCreditCardHolder();
                   LOG.info(msg);
                   LCUtil.showMessageFatal(msg);
                   return false;
                 } //end if

        }catch (SQLException sqle) {
            String msg = "££££ SQLException in ModifyCreditcard = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode()
                    + " player = " + player.getIdplayer();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in ModifyCreditcard = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//return false;
 } //end method
 
} //end class