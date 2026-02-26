package rowmappers;

import entite.LessonPayment;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class LessonPaymentRowMapper extends AbstractRowMapper<LessonPayment> {

    @Override
    public LessonPayment map(ResultSet rs) throws SQLException {
   //     final String methodName = utils.LCUtil.getCurrentMethodName();
     try {
            LessonPayment lp = new LessonPayment();
        //        LOG.debug("starting LessonPayment");
            lp.setPaymentStartDate(getTimestamp(rs,"LessonStartDate").toLocalDateTime());
            lp.setPaymentEndDate(getTimestamp(rs,"LessonEndDate").toLocalDateTime());
            lp.setPaymentDate(getTimestamp(rs,"LessonModificationDate").toLocalDateTime());
            lp.setPaymentCommunication(getString(rs,"LessonCommunication"));
            lp.setPaymentAmount(getDouble(rs,"LessonAmount"));
            lp.setPaymentIdStudent(getInteger(rs,"LessonIdStudent")); // mod 02-02-2023
            lp.setPaymentIdClub(getInteger(rs,"LessonIdClub"));
       //         LOG.debug("PaymentIdStudent = " +  lp.getPaymentIdStudent());
       //     lp.setEventEndDate(rs.getTimestamp("EventEndDate").toLocalDateTime());
            
       //     lp.setEventProId(rs.getInt("EventProId"));
       //     lp.setEventPlayerId(rs.getInt("EventPlayerId"));
      //      event.setEventAllDay(rs.getBoolean("EventAllDay"));
       //     event.setEventTitle(rs.getString("EventTitle"));
       //     event.setEventDescription(rs.getString("EventDescription"));
 //              LOG.debug("ScheduleEvent event returned from map = " + event);
            return lp;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    }
} // end class