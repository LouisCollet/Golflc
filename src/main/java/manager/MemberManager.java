package manager;

import entite.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import entite.composite.ECourseList;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static utils.LCUtil.showMessageInfo;

/**
 * Service metier pour la gestion des membres : tarifs, paiements, subscriptions
 * Delegue aux services CRUD et gere l'invalidation des caches
 */
@ApplicationScoped
public class MemberManager implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // INJECTIONS CDI - Services CRUD
    // ========================================

    // Create
    @Inject private create.CreateTarifMember         createTarifMemberService;
    @Inject private create.CreateTarifGreenfee       createTarifGreenfeeService;
    @Inject private create.CreatePaymentCotisation   createPaymentCotisationService;
    @Inject private create.CreatePaymentGreenfee     createPaymentGreenfeeService;
    @Inject private create.CreatePaymentSubscription createPaymentSubscriptionService;

    // Delete
    @Inject private delete.DeleteTarifMember         deleteTarifMemberService;
    @Inject private delete.DeleteTarifGreenfee       deleteTarifGreenfeeService;
    @Inject private delete.DeletePaymentGreenfee     deletePaymentGreenfeeService;
    @Inject private delete.DeleteSubscription        deleteSubscriptionService;

    // Update
    @Inject private update.UpdateSubscription        updateSubscriptionService;

    // Find
    @Inject private find.FindTarifMembersData        findTarifMembersDataService;
    @Inject private find.FindTarifGreenfeeData       findTarifGreenfeeDataService;
    @Inject private find.FindSubscriptionStatus      findSubscriptionStatusService;
    @Inject private find.FindCotisationAtRoundDate   findCotisationAtRoundDateService;
    @Inject private find.FindGreenfeePaid            findGreenfeePaidService;
    @Inject private find.FindCurrentSubscription     findCurrentSubscriptionService;
    @Inject private find.FindTarifMembersOverlapping findTarifMembersOverlappingService;

    // Business logic
    @Inject private Controllers.TarifMemberController   tarifMemberController;
    @Inject private Controllers.TarifGreenfeeController  tarifGreenfeeController;

    // Lists (pour cache invalidation)
    @Inject private lists.LocalAdminCotisationList     localAdminCotisationList;
    @Inject private lists.LocalAdminGreenfeeList       localAdminGreenfeeList;
    @Inject private lists.SubscriptionRenewalList      subscriptionRenewalList;
    @Inject private lists.SystemAdminSubscriptionList  systemAdminSubscriptionList;

    public MemberManager() { }

    // ========================================
    // TARIF MEMBER
    // ========================================

    public SaveResult createTarifMember(TarifMember tarif) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarif == null) {
            return SaveResult.failure("TarifMember cannot be null");
        }
        try {
            boolean success = createTarifMemberService.create(tarif);
            if (success) {
                String msg = "TarifMember created for club " + tarif.getTarifMemberIdClub();
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("TarifMember creation failed");
            }
        } catch (SQLException e) {
            LOG.error("SQLException creating TarifMember", e);
            return SaveResult.failure("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Exception creating TarifMember", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    public SaveResult deleteTarifMember(TarifMember tarif) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarif == null) {
            return SaveResult.failure("TarifMember cannot be null");
        }
        try {
            boolean success = deleteTarifMemberService.delete(tarif);
            if (success) {
                String msg = "TarifMember deleted";
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("TarifMember deletion failed");
            }
        } catch (Exception e) {
            LOG.error("Exception deleting TarifMember", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    // ========================================
    // TARIF GREENFEE
    // ========================================

    public SaveResult createTarifGreenfee(TarifGreenfee tarif, Club club) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarif == null) {
            return SaveResult.failure("TarifGreenfee cannot be null");
        }
        try {
            boolean success = createTarifGreenfeeService.create(tarif, club);
            if (success) {
                String msg = "TarifGreenfee created";
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("TarifGreenfee creation failed");
            }
        } catch (Exception e) {
            LOG.error("Exception creating TarifGreenfee", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    public SaveResult deleteTarifGreenfee(TarifGreenfee tarif, String year) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarif == null) {
            return SaveResult.failure("TarifGreenfee cannot be null");
        }
        try {
            boolean success = deleteTarifGreenfeeService.delete(tarif, year);
            if (success) {
                String msg = "TarifGreenfee deleted for year " + year;
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("TarifGreenfee deletion failed");
            }
        } catch (Exception e) {
            LOG.error("Exception deleting TarifGreenfee", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    // ========================================
    // PAYMENTS
    // ========================================

    public SaveResult createPaymentCotisation(Cotisation cotisation) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (cotisation == null) {
            return SaveResult.failure("Cotisation cannot be null");
        }
        try {
            boolean success = createPaymentCotisationService.create(cotisation);
            if (success) {
                // localAdminCotisationList.invalidateCache(); // migrated 2026-02-24
                String msg = "Payment cotisation created";
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Payment cotisation creation failed");
            }
        } catch (SQLException e) {
            LOG.error("SQLException creating payment cotisation", e);
            return SaveResult.failure("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Exception creating payment cotisation", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    public SaveResult createPaymentGreenfee(Player player, Greenfee greenfee) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (player == null || greenfee == null) {
            return SaveResult.failure("Player and Greenfee cannot be null");
        }
        try {
            boolean success = createPaymentGreenfeeService.create(player, greenfee);
            if (success) {
                // localAdminGreenfeeList.invalidateCache(); // migrated 2026-02-24
                String msg = "Payment greenfee created for player " + player.getIdplayer();
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Payment greenfee creation failed");
            }
        } catch (SQLException e) {
            LOG.error("SQLException creating payment greenfee", e);
            return SaveResult.failure("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Exception creating payment greenfee", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    public SaveResult createPaymentSubscription(Subscription subscription) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (subscription == null) {
            return SaveResult.failure("Subscription cannot be null");
        }
        try {
            boolean success = createPaymentSubscriptionService.create(subscription);
            if (success) {
                // subscriptionRenewalList.invalidateCache(); // migrated 2026-02-24
                // systemAdminSubscriptionList.invalidateCache(); // migrated 2026-02-24
                String msg = "Payment subscription created for player " + subscription.getIdplayer();
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Payment subscription creation failed");
            }
        } catch (SQLException e) {
            LOG.error("SQLException creating payment subscription", e);
            return SaveResult.failure("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Exception creating payment subscription", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    // ========================================
    // SUBSCRIPTION
    // ========================================

    public SaveResult deleteSubscription(Subscription subscription) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (subscription == null) {
            return SaveResult.failure("Subscription cannot be null");
        }
        try {
            boolean success = deleteSubscriptionService.delete(subscription);
            if (success) {
                // subscriptionRenewalList.invalidateCache(); // migrated 2026-02-24
                // systemAdminSubscriptionList.invalidateCache(); // migrated 2026-02-24
                String msg = "Subscription deleted for player " + subscription.getIdplayer();
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Subscription deletion failed");
            }
        } catch (Exception e) {
            LOG.error("Exception deleting subscription", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    public SaveResult updateSubscription(Subscription subscription) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (subscription == null) {
            return SaveResult.failure("Subscription cannot be null");
        }
        try {
            boolean success = updateSubscriptionService.modify(subscription);
            if (success) {
                // subscriptionRenewalList.invalidateCache(); // migrated 2026-02-24
                // systemAdminSubscriptionList.invalidateCache(); // migrated 2026-02-24
                String msg = "Subscription updated for player " + subscription.getIdplayer();
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Subscription update failed");
            }
        } catch (Exception e) {
            LOG.error("Exception updating subscription", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    public SaveResult deletePaymentGreenfee(Player player, Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (player == null || round == null) {
            return SaveResult.failure("Player and Round cannot be null");
        }
        try {
            boolean success = deletePaymentGreenfeeService.delete(player, round);
            if (success) {
                // localAdminGreenfeeList.invalidateCache(); // migrated 2026-02-24
                String msg = "Payment greenfee deleted for player " + player.getIdplayer();
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Payment greenfee deletion failed");
            }
        } catch (Exception e) {
            LOG.error("Exception deleting payment greenfee", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    // ========================================
    // FIND - Tarifs & Subscription
    // ========================================

    public TarifMember findTarifMembersData(Club club, Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return findTarifMembersDataService.find(club, round);
        } catch (SQLException e) {
            LOG.error("SQLException in " + methodName, e);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public TarifGreenfee findTarifGreenfeeData(Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return findTarifGreenfeeDataService.find(round);
        } catch (SQLException e) {
            LOG.error("SQLException in " + methodName, e);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public Boolean findSubscriptionStatus(Subscription subscription, Player player) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return findSubscriptionStatusService.find(subscription, player);
        } catch (SQLException e) {
            LOG.error("SQLException in " + methodName, e);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public boolean findGreenfeePaid(Player player, Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return findGreenfeePaidService.find(player, round);
        } catch (SQLException e) {
            LOG.error("SQLException in " + methodName, e);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Cotisation findCotisationAtRoundDate(Player player, Club club, Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return findCotisationAtRoundDateService.find(player, club, round);
        } catch (SQLException e) {
            LOG.error("SQLException in " + methodName, e);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public boolean findTarifMembersOverlapping(TarifMember tarif) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return findTarifMembersOverlappingService.find(tarif);
        } catch (SQLException e) {
            LOG.error("SQLException in " + methodName, e);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ========================================
    // TARIF BUSINESS LOGIC - Delegation
    // ========================================

    public TarifMember inputTarifMembersCotisation(TarifMember tarifMember) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return tarifMemberController.inputTarifMembersCotisation(tarifMember);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public TarifMember inputTarifMembersEquipments(TarifMember tarifMember) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return tarifMemberController.inputTarifMembersEquipments(tarifMember);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public Cotisation completeCotisation(TarifMember tarif, Player player, java.time.LocalDate referenceDate) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return tarifMemberController.completeCotisation(tarif, player, referenceDate);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public Greenfee completeGreenfee(TarifGreenfee tarif, Club club, Round round, Player player) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return tarifGreenfeeController.completeGreenfee(tarif, club, round, player);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // LISTS
    // ========================================

    public List<ECourseList> listSystemAdminSubscriptions() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            List<ECourseList> result = systemAdminSubscriptionList.list();
            return result != null ? result : Collections.emptyList();
        } catch (SQLException e) {
            LOG.error("SQLException in " + methodName, e);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listLocalAdminCotisations(Player localAdmin) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            List<ECourseList> result = localAdminCotisationList.list(localAdmin);
            return result != null ? result : Collections.emptyList();
        } catch (SQLException e) {
            LOG.error("SQLException in " + methodName, e);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listLocalAdminGreenfees(Player localAdmin) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            List<ECourseList> result = localAdminGreenfeeList.list(localAdmin);
            return result != null ? result : Collections.emptyList();
        } catch (SQLException e) {
            LOG.error("SQLException in " + methodName, e);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // SAVE RESULT PATTERN
    // ========================================

    public static class SaveResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private final boolean success;
        private final String message;

        private SaveResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static SaveResult success(String message) {
            return new SaveResult(true, message);
        }

        public static SaveResult failure(String message) {
            return new SaveResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("SaveResult{success=%s, message='%s'}", success, message);
        }
    } // end SaveResult

} // end class
