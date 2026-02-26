
package payment;

import entite.Club;
import entite.Creditcard;
import entite.Player;
import static interfaces.Log.LOG;

public final class SubscriptionRegistrar implements PaymentRegistrar<SubscriptionPayment> {

    private final Creditcard creditcard;
    private final Player player;
    private final Club club;
    private final PaymentSubscriptionController paymentSubscriptionController;

    public SubscriptionRegistrar(
            Creditcard creditcard,
            Player player,
            Club club,
            PaymentSubscriptionController paymentSubscriptionController) {
        this.creditcard = creditcard;
        this.player = player;
        this.club = club;
        this.paymentSubscriptionController = paymentSubscriptionController;
    }

    @Override
    public boolean register(SubscriptionPayment payment) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // return new payment.PaymentSubscriptionController().createPayment(payment.subscription(), conn);
        return paymentSubscriptionController.createPayment(payment.subscription()); // migrated 2026-02-25
    } // end method
} // end class
