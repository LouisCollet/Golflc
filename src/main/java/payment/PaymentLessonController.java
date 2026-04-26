package payment;

import entite.Club;
import entite.Creditcard;
import entite.Lesson;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class PaymentLessonController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private create.CreateLesson createLesson;

    public PaymentLessonController() { }

    public boolean registerPayment(Creditcard creditcard, List<Lesson> lessons,
                                   Player player, Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - lessons=" + lessons.size()
                + " player=" + player.getIdplayer()
                + " club=" + club.getIdclub()
                + " reference=" + creditcard.getCreditcardPaymentReference());
        try {
            for (Lesson lesson : lessons) {
                if (!createLesson.create(lesson, player)) {
                    LOG.error(methodName + " - failed to persist lesson: " + lesson);
                    return false;
                }
            }
            LOG.info(methodName + " - all lessons persisted to DB, reference="
                    + creditcard.getCreditcardPaymentReference());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
