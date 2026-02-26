package find;

import entite.Player;
import entite.Subscription;
import static interfaces.Log.LOG;
import static exceptions.LCException.handleGenericException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import utils.LCUtil;

@ApplicationScoped
public class FindSubscriptionStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.FindCurrentSubscription findCurrentSubscription;

    public FindSubscriptionStatus() { }

    public Boolean find(Subscription subscription, Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - with subscription idplayer = " + subscription.getIdplayer());

        try {
            List<Subscription> subscriptionList = findCurrentSubscription.payments(player, "now");
            if (subscriptionList == null || subscriptionList.isEmpty()) {
                String msg = LCUtil.prepareMessageBean("subscription.notfound");
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return false;
            }

            LOG.debug("subscription detail found = " + Arrays.deepToString(subscriptionList.toArray()));

            subscription = subscriptionList.get(0);
            LOG.debug("current subscription = " + subscription);

            if (subscription.getTrialCount() > 5 && LocalDateTime.now().isAfter(subscription.getEndDate())) {
                String msg = LCUtil.prepareMessageBean("subscription.create.toomuchtrials")
                        + " player = " + player.getIdplayer()
                        + " , trial  = <h1>" + subscription.getTrialCount() + "</h1>";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                LOG.debug("returned to subscription.xhtml");
                return false;
            }

            if (LocalDateTime.now().isBefore(subscription.getStartDate())) {
                String msg = "now is before subscription Start - subscription NOT valid !!! " + subscription.getStartDate().format(DateTimeFormatter.ISO_DATE);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }

            if (LocalDateTime.now().isAfter(subscription.getEndDate())) {
                String msg = "now is after subscription endDate - subscription NOT valid !!! " + subscription.getEndDate().format(DateTimeFormatter.ISO_DATE);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }

            return true;

        } catch (Exception e) {
            String msg = "Exception in " + methodName + " " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
    } // end method

    /** @deprecated use {@link #find(Subscription, Player)} via CDI injection */
    /*
    void main() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(456989);
        Subscription subscription = new Subscription();
        Boolean p1 = find(subscription, player);
        LOG.debug("subscription found ? = " + p1.toString());
    } // end main
    */

} // end class
