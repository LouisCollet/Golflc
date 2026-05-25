package payment;

import entite.Player;
import entite.Subscription;
import entite.Subscription.etypeSubscription;
import static interfaces.Log.LOG;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static utils.LCUtil.prepareMessageBean;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentSubscriptionController implements Serializable {

    @Inject private find.FindCurrentSubscription findCurrentSubscription;
    @Inject private find.FindSubscriptionStatus findSubscriptionStatus;
    @Inject private create.CreatePaymentSubscription createPaymentSubscription;
    @Inject private find.FindTarifSubscription findTarifSubscription;

    public PaymentSubscriptionController() {}

    private Subscription completeTrial(Subscription subscription, Subscription previous) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Short t = previous.getTrialCount();
            subscription.setTrialCount((short) (t + 1));

            if (subscription.getTrialCount() > 5) {
                LOG.warn("trial limit reached player={} count={}", subscription.getIdplayer(), subscription.getTrialCount());
                return null;
            }

            LocalTime startTime = LocalTime.of(0, 0);
            LocalTime endTime = LocalTime.of(23, 59, 59);
            LocalDate now = LocalDate.now();

            subscription.setStartDate(LocalDateTime.of(now, startTime));
            subscription.setEndDate(LocalDateTime.of(now, endTime));

            return subscription;
        } catch (Exception ex) {
            LOG.error("exception in completeTrial", ex);
            return null;
        }
    } // end method

    private Subscription findLatestSubscription(int idplayer) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(idplayer);
        List<Subscription> list = findCurrentSubscription.payments(player, "latest");
        return (list == null || list.isEmpty()) ? null : list.get(0);
    } // end method

    private void initSubscription(Subscription subscription) {
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(subscription.getStartDate().plusMonths(1));
        subscription.setTrialCount((short) 1);
        subscription.setPaymentReference("Initial - No reference");
        subscription.setCommunication("Initial fake subscription");
    } // end method

    private Subscription setStartEndDates(Subscription subscription, Subscription previous) {
        if (previous != null && LocalDateTime.now().isBefore(previous.getEndDate())) {
            subscription.setStartDate(previous.getEndDate().plusDays(1));
        } else {
            subscription.setStartDate(LocalDateTime.now());
        }

        switch (subscription.getSubCode()) {
            case "MONTHLY" -> subscription.setEndDate(subscription.getStartDate().plusMonths(1));
            case "YEARLY" -> subscription.setEndDate(subscription.getStartDate().plusYears(1));
        }

        return subscription;
    } // end method

    public Subscription completePriceAndCommunication(Subscription subscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        double price = findTarif(subscription);
        subscription.setSubscriptionAmount(price);

        switch (subscription.getSubCode()) {
            case "MONTHLY" -> subscription.setCommunication(
                    prepareMessageBean("subscription.month") + " (" + price + ") "
                            + "period: " + subscription.getStartDate().format(DateTimeFormatter.ISO_DATE)
                            + " - " + subscription.getEndDate().format(DateTimeFormatter.ISO_DATE));
            case "YEARLY" -> subscription.setCommunication(
                    prepareMessageBean("subscription.year") + " (" + price + ")");
            case "TRIAL" -> {
                subscription.setPaymentReference("Trial - No reference");
                subscription.setCommunication(prepareMessageBean("subscription.trial") + " (" + price + ")");
            }
            case "INITIAL" -> {
                subscription.setPaymentReference("Initial - No reference");
                subscription.setTrialCount((short) 1);
                subscription.setCommunication("Initial subscription one month free");
            }
        }
        LOG.debug("subscription completed={}", subscription);
        return subscription;
    } // end method

    public Subscription complete(Subscription subscription) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try {
            Integer idplayer = subscription.getIdplayer();
            if (idplayer == null) {
                LOG.error("complete() — null idplayer subscription={}", subscription);
                return subscription;
            }
            Subscription latest = findLatestSubscription(idplayer);

            switch (subscription.getSubCode()) {
                case "INITIAL" -> initSubscription(subscription);
                case "TRIAL" -> {
                    if (latest != null) {
                        subscription = completeTrial(subscription, latest);
                        if (subscription == null) {
                            throw new Exception("Trial subscription limit reached");
                        }
                    }
                }
                default -> subscription = setStartEndDates(subscription, latest);
            }

            subscription = completePriceAndCommunication(subscription);

            if (!subscription.getSubCode().equals("TRIAL")) {
                subscription.setTrialCount((short) 0);
            }

            return subscription;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return subscription;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return subscription;
        }
    } // end method

    public boolean createPayment(Subscription subscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try {
            subscription = complete(subscription);
            boolean success = createPaymentSubscription.create(subscription);

            if (success) {
                LOG.info("payments_subscription created player={}", subscription.getIdplayer());
            } else {
                LOG.error("[SUBSCRIPTION] payments_subscription creation failed player={}", subscription.getIdplayer());
            }

            return success;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            throw e;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            throw e;
        }
    } // end method

    public double findTarif(Subscription subscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        return switch (subscription.getSubCode()) {
            case "MONTHLY", "YEARLY" -> {
                entite.TarifSubscription tarif = findTarifSubscription.findActive(subscription.getSubCode());
                if (tarif != null) {
                    LOG.debug("tarif found in DB tarif={}", tarif);
                    yield tarif.getPrice();
                }
                LOG.error("no active tarif in DB for subCode={}", subscription.getSubCode());
                yield 0.0;
            }
            case "TRIAL", "INITIAL" -> 0.0;
            default -> 99.0;
        };
    } // end method

    public Subscription isExists(Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        Subscription subscription = new Subscription();
        subscription.setIdplayer(player.getIdplayer());

        try {
            Subscription found = findSubscriptionStatus.find(subscription, player);
            if (found != null) {
                found.setErrorStatus(false);
                return found;
            } else {
                subscription.setErrorStatus(true);
                LOG.error(prepareMessageBean("subscription.invalid"));
            }

            return subscription;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return subscription;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return subscription;
        }
    } // end method

} // end class
