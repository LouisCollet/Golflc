package payment;

import entite.Player;
import entite.Subscription;
import entite.Subscription.etypeSubscription;
import static interfaces.Log.LOG;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import static utils.LCUtil.prepareMessageBean;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentSubscriptionController implements Serializable, interfaces.Log {

    @Inject private find.FindCurrentSubscription findCurrentSubscription;
    @Inject private find.FindSubscriptionStatus findSubscriptionStatus;
    @Inject private create.CreatePaymentSubscription createPaymentSubscription;
    @Inject private find.FindTarifSubscription findTarifSubscription;

    public PaymentSubscriptionController() {}

    /**
     * Complète un abonnement trial avec les informations du dernier abonnement.
     */
    private static Subscription completeTrial(Subscription subscription, Subscription previous) {
        try {
            LOG.debug("Entering completeTrial with subscription = {}", subscription);

            Short t = previous.getTrialCount();
            subscription.setTrialCount((short) (t + 1));

            LocalTime startTime = LocalTime.of(0, 0);
            LocalTime endTime = LocalTime.of(23, 59, 59);
            LocalDate now = LocalDate.now();

            subscription.setStartDate(LocalDateTime.of(now, startTime));
            subscription.setEndDate(LocalDateTime.of(now, endTime));

            if (subscription.getTrialCount() > 5 && LocalDateTime.now().isAfter(subscription.getEndDate())) {
                String msg = prepareMessageBean("subscription.create.toomuchtrials")
                        + " player = " + subscription.getIdplayer()
                        + " , trial  = <h1>" + subscription.getTrialCount() + "</h1>";
                LOG.error(msg);
                showMessageFatal(msg);
            }

            return subscription;
        } catch (Exception ex) {
            LOG.error("Exception in completeTrial: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Trouve le dernier abonnement d’un joueur.
     */
    private Subscription findLatestSubscription(int idplayer) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(idplayer);
        List<Subscription> list = findCurrentSubscription.payments(player, "latest"); // migrated 2026-02-25
        return (list == null || list.isEmpty()) ? null : list.get(0);
    } // end method

    /**
     * Initialise un abonnement INITIAL.
     */
    private void initSubscription(Subscription subscription) { // payement initial dans createPlayer
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(subscription.getStartDate().plusMonths(1));
        subscription.setTrialCount((short) 1);
        subscription.setPaymentReference("Initial - No reference");
        subscription.setCommunication("Initial fake subscription");

    }

    /**
     * Définit startDate et endDate pour les abonnements non-trial.
     */
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
    }

    /**
     * Complète prix et communication selon le type d’abonnement.
     */
    public Subscription completePriceAndCommunication(Subscription subscription) throws Exception {
        LOG.debug("entering completePriceAndCommunication");
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
             //   subscription.setCommunication(prepareMessageBean("subscription.initial"));
                subscription.setCommunication("Initial subscription one month free");
            }
        }
        LOG.debug("subscription completed = " + subscription);
        return subscription;
    }

    /**
     * Complète un abonnement (dates, prix, communication, trial).
     */
    public Subscription complete(Subscription subscription) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        try {
            Subscription latest = findLatestSubscription(subscription.getIdplayer()); // migrated 2026-02-25

            switch (subscription.getSubCode()) {
                case "INITIAL" -> initSubscription(subscription);
                case "TRIAL" -> {
                    if (latest != null) {
                        subscription = completeTrial(subscription, latest);
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

    /**
     * Crée un paiement pour un abonnement.
     */
    public boolean createPayment(Subscription subscription) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        try {
            subscription = complete(subscription); // migrated 2026-02-25
            boolean success = createPaymentSubscription.create(subscription); // migrated 2026-02-25

            if (success) {
                showMessageInfo(prepareMessageBean("subscription.success") + subscription);
            } else {
                showMessageFatal("Error: payment subscription NOT done!");
            }

            return success;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /**
     * Retourne le tarif d’un abonnement selon son type.
     * Lit le prix depuis la table tarif_subscription (tarif actif pour le code).
     * Fallback sur le fichier subscription.properties si pas trouvé en DB.
     */
    public double findTarif(Subscription subscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        return switch (subscription.getSubCode()) {
            case "MONTHLY", "YEARLY" -> {
                entite.TarifSubscription tarif = findTarifSubscription.findActive(subscription.getSubCode());
                if (tarif != null) {
                    LOG.debug(methodName + " - tarif found in DB: " + tarif);
                    yield tarif.getPrice();
                }
                LOG.error(methodName + " - no active tarif in DB for " + subscription.getSubCode());
                showMessageFatal("No active tarif found for " + subscription.getSubCode());
                yield 0.0;
            }
            case "TRIAL", "INITIAL" -> 0.0;
            default -> 99.0;
        };
    }

    /**
     * Vérifie si un abonnement existe pour un joueur.
     */
    public Subscription isExists(Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        Subscription subscription = new Subscription();
        subscription.setIdplayer(player.getIdplayer());

        try {
            boolean found = findSubscriptionStatus.find(subscription, player); // migrated 2026-02-25
            if (found) {
                List<Subscription> list = findCurrentSubscription.payments(player, "now"); // migrated 2026-02-25
                subscription = list.isEmpty() ? subscription : list.get(0);
                subscription.setErrorStatus(false);
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
