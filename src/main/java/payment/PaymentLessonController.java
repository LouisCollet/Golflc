package payment;

import entite.Club;
import entite.Creditcard;
import entite.Lesson;
import entite.Player;
import entite.Professional;
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

    @Inject private create.CreateLesson        createLesson;
    @Inject private create.CreatePaymentLesson createPaymentLesson;

    public PaymentLessonController() { }

    public boolean registerPayment(Creditcard creditcard, List<Lesson> lessons,
                                   Professional professional, Player player, Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("lessons={} player={} club={} reference={}",
                lessons.size(), player.getIdplayer(), club.getIdclub(),
                creditcard.getCreditcardPaymentReference());
        try {
            // 1. Persister chaque leçon (réservation créneau)
            for (Lesson lesson : lessons) {
                if (!createLesson.create(lesson, player)) {
                    LOG.error("failed to persist lesson: {}", lesson);
                    return false;
                }
            }
            LOG.info("all lessons persisted to DB, reference={}", creditcard.getCreditcardPaymentReference());

            // 2. UN seul enregistrement payments_lesson pour toute la transaction
            if (professional == null) {
                LOG.warn("professional is null — payments_lesson skipped");
                return true;
            }
            createPaymentLesson.create(lessons, creditcard, professional, creditcard.getCreditcardPaymentReference());
            LOG.info("payments_lesson persisted reference={}", creditcard.getCreditcardPaymentReference());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
