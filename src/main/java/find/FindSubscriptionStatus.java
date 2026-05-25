package find;

import entite.Player;
import entite.Subscription;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import utils.LCUtil;

@ApplicationScoped
public class FindSubscriptionStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.FindCurrentSubscription findCurrentSubscription;

    public FindSubscriptionStatus() { }

    public Subscription find(Subscription subscription, Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("{} - with subscription idplayer = {}", methodName, subscription.getIdplayer());

        try {
            List<Subscription> subscriptionList = findCurrentSubscription.payments(player, "now");
            if (subscriptionList == null || subscriptionList.isEmpty()) {
                String msg = LCUtil.prepareMessageBean("subscription.notfound");
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return null;
            }

            Subscription found = subscriptionList.get(0);
            LOG.debug("subscription found = {}", found);

            if (found.getTrialCount() > 5 && LocalDateTime.now().isAfter(found.getEndDate())) {
                String msg = LCUtil.prepareMessageBean("subscription.create.toomuchtrials")
                        + " player = " + player.getIdplayer()
                        + " , trial  = <h1>" + found.getTrialCount() + "</h1>";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            }

            if (LocalDateTime.now().isBefore(found.getStartDate())) {
                String msg = "now is before subscription Start - subscription NOT valid !!! " + found.getStartDate().format(DateTimeFormatter.ISO_DATE);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            }

            if (LocalDateTime.now().isAfter(found.getEndDate())) {
                String msg = "now is after subscription endDate - subscription NOT valid !!! " + found.getEndDate().format(DateTimeFormatter.ISO_DATE);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            }

            return found;

        } catch (Exception e) {
            String msg = "Exception in " + methodName + " " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }
    } // end method

    /** @deprecated use {@link #find(Subscription, Player)} via CDI injection */
    /*
    void main() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(456989);
        Subscription subscription = new Subscription();
        Boolean p1 = find(subscription, player);
        LOG.debug("subscription found ? = " + p1.toString());
    } // end main
    */

} // end class
