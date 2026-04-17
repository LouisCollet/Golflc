
package rowmappers;

import entite.Subscription;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubscriptionRowMapper extends AbstractRowMapper<Subscription> {
// public class PlayerRowMapper extends AbstractRowMapper<Player> {
    @Override
   public Subscription map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();  
   try{  
           //LOG.debug("entering map for method = " + methodName);
        Subscription subscription = new Subscription();
        subscription.setIdplayer(getInteger(rs,"SubscriptionIdPlayer") );
        subscription.setStartDate(getTimestamp(rs,"SubscriptionStartDate").toLocalDateTime());
        subscription.setEndDate(getTimestamp(rs,"SubscriptionEndDate").toLocalDateTime());
        subscription.setTrialCount(getShort(rs,"SubscriptionTrialCount"));
        subscription.setPaymentReference(getString(rs,"SubscriptionPaymentReference"));
        subscription.setCommunication(getString(rs,"SubscriptionCommunication"));
        subscription.setSubscriptionAmount(getDouble(rs,"SubscriptionAmount"));
        subscription.setPaymentDate(getTimestamp(rs,"SubscriptionModificationDate").toLocalDateTime());
   return subscription;
 }catch(Exception e){
        handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class