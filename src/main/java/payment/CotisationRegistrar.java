package payment;

import entite.Club;
import entite.Course;
import entite.Creditcard;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;

public final class CotisationRegistrar implements PaymentRegistrar<CotisationPayment> {

    private final Creditcard creditcard;
    private final Player player;
    private final Round round;
    private final Club club;
    private final Course course;
    private final Inscription inscription;
    private final PaymentCotisationController paymentCotisationController;

    public CotisationRegistrar(Creditcard creditcard, Player player, Round round,
                               Club club, Course course, Inscription inscription,
                               PaymentCotisationController paymentCotisationController) {
        this.creditcard = creditcard;
        this.player = player;
        this.round = round;
        this.club = club;
        this.course = course;
        this.inscription = inscription;
        this.paymentCotisationController = paymentCotisationController;
    }

    @Override
    public boolean register(CotisationPayment payment) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return paymentCotisationController.RegisterPaymentandInscription(
                creditcard,
                payment.cotisation(),
                player,
                round,
                club,
                course,
                inscription
        );
    } // end method

} // end class
