package payment;

import entite.Club;
import entite.Creditcard;
import entite.Player;

public final class LessonRegistrar implements PaymentRegistrar<LessonPayment> {

    private final Creditcard creditcard;
    private final Player player;
    private final Club club;
    private final PaymentLessonController paymentLessonController;

    public LessonRegistrar(Creditcard creditcard, Player player, Club club,
                           PaymentLessonController paymentLessonController) {
        this.creditcard = creditcard;
        this.player = player;
        this.club = club;
        this.paymentLessonController = paymentLessonController;
    }

    @Override
    public boolean register(LessonPayment payment) throws Exception {
        return paymentLessonController.registerPayment(creditcard, payment.lessons(), player, club);
    }

} // end class
