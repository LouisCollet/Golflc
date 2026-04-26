package sql.preparedstatement;

import entite.Creditcard;
import entite.Lesson;
import entite.Professional;
import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class psCreatePaymentLesson implements Serializable, interfaces.Log, interfaces.GolfInterface {

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final List<Lesson> lessons,
            final Creditcard creditcard,
            final Professional professional,
            final String lessonCommunication) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            Lesson first = lessons.get(0);
            Lesson last  = lessons.get(lessons.size() - 1);

            ps.setNull(1, java.sql.Types.INTEGER);                               // AUTO-INCREMENT
            ps.setInt(2, professional.getProId());                               // LessonIdPro
            ps.setInt(3, professional.getProClubId());                           // LessonIdClub
            ps.setInt(4, creditcard.getCreditCardIdPlayer());                    // LessonIdStudent
            ps.setTimestamp(5, Timestamp.valueOf(first.getEventStartDate()));    // LessonStartDate
            ps.setTimestamp(6, Timestamp.valueOf(last.getEventEndDate()));       // LessonEndDate
            ps.setString(7, creditcard.getCreditcardPaymentReference());         // LessonPaymentReference
            ps.setString(8, lessonCommunication);                                // LessonCommunication
            ps.setDouble(9, creditcard.getTotalPrice());                         // LessonAmount
            ps.setTimestamp(10, Timestamp.from(Instant.now()));                  // LessonModificationDate
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
