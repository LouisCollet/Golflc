package update;

import entite.Creditcard;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyCreditcard{
public boolean modify(Creditcard creditcard , final Connection conn) throws SQLException {
   PreparedStatement ps = null;
  try {
            LOG.debug("starting modifyCreditcard");
            LOG.debug("with Creditcard =  " + creditcard.toString());
         
      final String query = """
            UPDATE creditcard
            SET CreditcardHolder = ?,
                CreditcardNumber = ?,
                CreditcardExpirationDate = ?,
                CreditcardType = ?,
                CreditcardVerificationCode = ?
            WHERE
                CreditcardIdPlayer=?
            """ ;
            
            ps = conn.prepareStatement(query);
            ps.setString(1,creditcard.getCreditcardHolder());
            ps.setString(2,creditcard.getCreditcardNumber());  // mod 11-04-2021
            ps.setTimestamp(3,Timestamp.valueOf(creditcard.getCreditCardExpirationDateLdt())); // mod 02-10-2021
            ps.setString(4,creditcard.getCreditcardType()); // VISA ...
            ps.setShort(5, creditcard.getCreditcardVerificationCode());
         //   ps.setInt(6,creditcard.getIdplayer());
             ps.setInt(6,creditcard.getCreditCardIdPlayer()); // mod 31-01-2023getCreditCardIdplayer());
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate();
                LOG.debug("rows = " + row);
            if (row != 0) {
                 String msg =  LCUtil.prepareMessageBean("creditcard.registered") + NEW_LINE + creditcard;
                 LOG.debug(msg);
                 LCUtil.showMessageInfo(msg);
                 return true;
             }else{
                   String msg = "NOT NOT Successful update, row = 0 "
                           + " player = " + creditcard.getCreditcardHolder();
                   LOG.debug(msg);
                   LCUtil.showMessageFatal(msg);
                   return false;
             } //end if

        }catch (SQLException sqle) {
            String msg = "££££ SQLException in ModifyCreditcard = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode()
                    + " player = " + creditcard.getCreditCardIdPlayer(); //mod 31-01-2023getCreditCardIdplayer();
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
 } //end method
} //end class