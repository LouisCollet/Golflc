package payment;

import entite.*;
import static interfaces.Log.LOG;
// import java.sql.Connection; // removed 2026-02-25

public final class PaymentOrchestrator {

    private final Creditcard creditcard;
    private final Player player;
    private final Round round;
    private final Club club;
    private final Course course;
    private final Inscription inscription;
    private final PaymentSubscriptionController paymentSubscriptionController;
    private final PaymentGreenfeeController paymentGreenfeeController; // replaces conn — migrated 2026-02-25

    public PaymentOrchestrator(Creditcard creditcard, Player player, Round round,
                               Club club, Course course, Inscription inscription,
                               PaymentSubscriptionController paymentSubscriptionController,
                               PaymentGreenfeeController paymentGreenfeeController) { // conn replaced 2026-02-25
        this.creditcard = creditcard;
        this.player = player;
        this.round = round;
        this.club = club;
        this.course = course;
        this.inscription = inscription;
        this.paymentSubscriptionController = paymentSubscriptionController;
        this.paymentGreenfeeController = paymentGreenfeeController;
    }

    public void handle(PaymentTarget target) throws Exception {
        LOG.debug("entering handle with target = " + target);
        target.setPaymentReference(creditcard.getCreditcardPaymentReference());

        boolean success = switch (target) {
            case CotisationPayment cp ->
                new CotisationRegistrar(creditcard, player, club).register(cp); // conn removed 2026-02-25
            case SubscriptionPayment sp ->
             //   new SubscriptionRegistrar(creditcard, player, club, conn).register(sp);
                new SubscriptionRegistrar(creditcard, player, club, paymentSubscriptionController).register(sp); // migrated 2026-02-25
            case GreenfeePayment gf ->
                new GreenfeeRegistrar(creditcard, player, round, club, course, inscription, paymentGreenfeeController).register(gf); // migrated 2026-02-25
            default -> throw new IllegalArgumentException("Type de paiement inconnu: " + target.getClass());
        };

        if (!success) {
            throw new Exception("Payment already registered for " + target);
        }
    }
}
