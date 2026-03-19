package create;

import entite.Creditcard;
import entite.Lesson;
import entite.Professional;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;

@ApplicationScoped
public class CreatePaymentLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private int generatedKey;

    public CreatePaymentLesson() { }

    public boolean create(
            final Lesson lesson,
            final Creditcard creditcard,
            final Professional professional) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("lesson  = " + lesson);
        LOG.debug("creditcard  = " + creditcard);
        LOG.debug("professional  = " + professional);

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "payments_lesson");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setNull(1, java.sql.Types.INTEGER); // AUTO-INCREMENT
                ps.setInt(2, professional.getProId());
                ps.setInt(3, professional.getProClubId());
                ps.setInt(4, creditcard.getCreditCardIdPlayer());
                ps.setTimestamp(5, Timestamp.valueOf(lesson.getEventStartDate()));
                ps.setTimestamp(6, Timestamp.valueOf(lesson.getEventEndDate()));
                ps.setString(7, creditcard.getCreditcardPaymentReference());
                ps.setString(8, creditcard.getCommunication());
                ps.setDouble(9, creditcard.getTotalPrice());
                ps.setTimestamp(10, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    generatedKey = LCUtil.generatedKey(conn);
                    LOG.debug("payment lesson, generated key = " + generatedKey);
                    String msg = "Payment Lesson created = </h1>" + lesson;
                    LOG.debug(msg);
                    return true;
                } else {
                    String msg = "<br/><br/>ERROR insert for lesson : ";
                    LOG.debug(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                String msg = LCUtil.prepareMessageBean("create.lesson.duplicate") + lesson;
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
            handleSQLException(sqle, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public int getGeneratedKey() {
        return generatedKey;
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class
