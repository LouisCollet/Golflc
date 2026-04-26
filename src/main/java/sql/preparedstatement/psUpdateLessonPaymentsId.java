package sql.preparedstatement;

import entite.Lesson;
import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class psUpdateLessonPaymentsId implements Serializable, interfaces.Log, interfaces.GolfInterface {

    /**
     * @param paymentsLessonId clé générée par l'INSERT dans payments_lesson
     * @param playerId          id du student (creditcard.getCreditCardIdPlayer())
     */
    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final int paymentsLessonId,
            final Lesson lesson,
            final int playerId) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setInt(1, paymentsLessonId);                                      // PaymentsLessonId
            ps.setInt(2, lesson.getEventProId());                                 // EventProId — WHERE
            ps.setInt(3, playerId);                                               // EventPlayerId — WHERE
            ps.setTimestamp(4, Timestamp.valueOf(lesson.getEventStartDate()));   // EventStartDate — WHERE
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
