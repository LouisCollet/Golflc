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

    public CreatePaymentLesson() { }

    /*
     * Enregistre UN paiement par leçon dans payments_lesson.
     * Une ligne par leçon avec son montant individuel.
     */
    public boolean create(
            final List<Lesson> lessons,
            final Creditcard creditcard,
            final Professional professional,
            final String baseReference) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("lessons count={} reference={}", lessons.size(), creditcard.getCreditcardPaymentReference());

        try (Connection conn = dao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                final String insertQuery = LCUtil.generateInsertQuery(conn, "payments_lesson");
                final String updateQuery = """
                    UPDATE lesson SET PaymentsLessonId = ?
                    WHERE EventProId = ? AND EventPlayerId = ? AND EventStartDate = ?
                    """;

                try (PreparedStatement psInsert = conn.prepareStatement(insertQuery);
                     PreparedStatement psUpdate = conn.prepareStatement(updateQuery)) {

                    for (Lesson lesson : lessons) {
                        String comm = "Student #" + creditcard.getCreditCardIdPlayer()
                            + "\n" + lesson.getEventStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                            + "→" + lesson.getEventEndDate().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                            + "\nRef: " + creditcard.getCreditcardPaymentReference();

                        sql.preparedstatement.psCreatePaymentLesson.psMapCreate(psInsert, lesson, creditcard, professional, comm);
                        int row = psInsert.executeUpdate();
                        if (row == 0) {
                            String msg = "[LESSON] ERROR insert payments_lesson for startDate=" + lesson.getEventStartDate();
                            LOG.error(msg);
                            LCUtil.showMessageFatal(msg);
                            conn.rollback();
                            return false;
                        }
                        int generatedKey = LCUtil.generatedKey(conn);
                        LOG.debug("payments_lesson inserted generatedKey={} startDate={}", generatedKey, lesson.getEventStartDate());

                        sql.preparedstatement.psUpdateLessonPaymentsId.psMapUpdate(
                            psUpdate, generatedKey, lesson, creditcard.getCreditCardIdPlayer());
                        int updated = psUpdate.executeUpdate();
                        LOG.debug("PaymentsLessonId={} set on lesson proId={} playerId={} start={} rows={}",
                            generatedKey, lesson.getEventProId(), creditcard.getCreditCardIdPlayer(),
                            lesson.getEventStartDate(), updated);
                    }
                }

                conn.commit();
                LOG.info("payments_lesson committed count={} reference={}", lessons.size(), creditcard.getCreditcardPaymentReference());
                return true;

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                String msg = "[LESSON] " + LCUtil.prepareMessageBean("create.lesson.duplicate");
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

} // end class
