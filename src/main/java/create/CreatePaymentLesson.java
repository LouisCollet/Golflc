package create;

import entite.Creditcard;
import entite.Lesson;
import entite.Professional;
import java.util.List;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class CreatePaymentLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private int generatedKey;

    public CreatePaymentLesson() { }

    /**
     * Enregistre UN paiement groupé pour toutes les leçons.
     * Une seule ligne dans payments_lesson par transaction de paiement.
     *
     * @param lessons             toutes les leçons payées dans cette transaction
     * @param creditcard          creditcard avec référence de paiement et montant total
     * @param professional        pro concerné
     * @param lessonCommunication détail regroupé : dates de toutes les leçons + id étudiant
     */
    public boolean create(
            final List<Lesson> lessons,
            final Creditcard creditcard,
            final Professional professional,
            final String lessonCommunication) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("lessons count = {}", lessons.size());
        LOG.debug("creditcard  = {}", creditcard);
        LOG.debug("professional  = {}", professional);
        LOG.debug("lessonCommunication = {}", lessonCommunication);

        // startDate = première leçon, endDate = dernière leçon
  //      Lesson first = lessons.get(0);
  //      Lesson last  = lessons.get(lessons.size() - 1);

        try (Connection conn = dao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // INSERT payments_lesson
                final String insertQuery = LCUtil.generateInsertQuery(conn, "payments_lesson");
                try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                    sql.preparedstatement.psCreatePaymentLesson.psMapCreate(ps, lessons, creditcard, professional, lessonCommunication);
                    utils.LCUtil.logps(ps);
                    int row = ps.executeUpdate();
                    if (row == 0) {
                        String msg = "ERROR insert payments_lesson";
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
                        conn.rollback();
                        return false;
                    }
                    generatedKey = LCUtil.generatedKey(conn);
                    LOG.debug("payment lesson, generated key = {}", generatedKey);
                }

                // UPDATE lesson SET PaymentsLessonId = generatedKey for each lesson in the cart
                final String updateQuery = """
                    UPDATE lesson SET PaymentsLessonId = ?
                    WHERE EventProId = ? AND EventPlayerId = ? AND EventStartDate = ?
                    """;
                try (PreparedStatement psUpdate = conn.prepareStatement(updateQuery)) {
                    for (Lesson lesson : lessons) {
                        sql.preparedstatement.psUpdateLessonPaymentsId.psMapUpdate(psUpdate, generatedKey, lesson, creditcard.getCreditCardIdPlayer());
                        utils.LCUtil.logps(psUpdate);
                        int updated = psUpdate.executeUpdate();
                        LOG.debug("PaymentsLessonId={} for proId={} playerId={} start={} rows={}",
                            generatedKey, lesson.getEventProId(),
                            creditcard.getCreditCardIdPlayer(), lesson.getEventStartDate(), updated);
                    }
                }

                conn.commit();
                LOG.debug("payment + lesson paid committed, generatedKey={}", generatedKey);
                return true;

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                String msg = LCUtil.prepareMessageBean("create.lesson.duplicate");
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
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
