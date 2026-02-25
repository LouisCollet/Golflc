
package payment;

import entite.Club;
import entite.Course;
import entite.Creditcard;
import entite.Inscription;
import entite.Player;
import entite.Round;
// import java.sql.Connection; // removed 2026-02-25

public final class GreenfeeRegistrar
        implements PaymentRegistrar<GreenfeePayment> {

    private final Creditcard creditcard;
    private final Player player;
    private final Round round;
    private final Club club;
    private final Course course;
    private final Inscription inscription;
    private final PaymentGreenfeeController paymentGreenfeeController; // replaces conn — migrated 2026-02-25

    public GreenfeeRegistrar(
            Creditcard creditcard,
            Player player,
            Round round,
            Club club,
            Course course,
            Inscription inscription,
            PaymentGreenfeeController paymentGreenfeeController) { // conn replaced by CDI service 2026-02-25

        this.creditcard = creditcard;
        this.player = player;
        this.round = round;
        this.club = club;
        this.course = course;
        this.inscription = inscription;
        this.paymentGreenfeeController = paymentGreenfeeController;
    }

    @Override
    public boolean register(GreenfeePayment payment) throws Exception {

        return paymentGreenfeeController
            .RegisterPaymentandInscription(
                creditcard,
                payment.greenfee(),
                player,
                round,
                club,
                course,
                inscription
            );
    }
}
