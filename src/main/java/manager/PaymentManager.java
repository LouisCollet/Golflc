package manager;

import context.ApplicationContext;
import entite.Creditcard;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Manager metier pour les paiements et cartes de credit.
 * Delegue aux services CRUD CDI existants.
 *
 * @author GolfLC
 */
@ApplicationScoped
public class PaymentManager implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // INJECTIONS CDI
    // ========================================

    @Inject private create.CreateCreditcard   createCreditcardService;
    @Inject private read.ReadCreditcard       readCreditcardService;
    @Inject private update.ModifyCreditcard   modifyCreditcardService;
    @Inject private mail.CreditcardMail       creditcardMail;
    @Inject private ApplicationContext        appContext;

    public PaymentManager() { }

    // ========================================
    // CREDITCARD — CRUD
    // ========================================

    /**
     * Cree une carte de credit en base.
     */
    public SaveResult createCreditcard(Creditcard creditcard) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            boolean created = createCreditcardService.create(creditcard);
            if (created) {
                LOG.debug(methodName + " - creditcard created for player " + creditcard.getCreditCardIdPlayer());
                return SaveResult.success("Creditcard created successfully");
            } else {
                LOG.warn(methodName + " - creditcard creation failed");
                return SaveResult.failure("Creditcard creation failed");
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return SaveResult.failure("SQL error during creditcard creation");
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return SaveResult.failure("Error during creditcard creation");
        }
    } // end method

    /**
     * Lit la carte de credit d'un joueur.
     */
    public Creditcard readCreditcard(Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Creditcard cc = readCreditcardService.read(player);
            LOG.debug(methodName + " - creditcard loaded = " + cc);
            return cc;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Modifie une carte de credit existante.
     */
    public SaveResult modifyCreditcard(Creditcard creditcard) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            boolean modified = modifyCreditcardService.modify(creditcard);
            if (modified) {
                LOG.debug(methodName + " - creditcard modified for player " + creditcard.getCreditCardIdPlayer());
                return SaveResult.success("Creditcard modified successfully");
            } else {
                LOG.warn(methodName + " - creditcard modification failed");
                return SaveResult.failure("Creditcard modification failed");
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return SaveResult.failure("SQL error during creditcard modification");
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return SaveResult.failure("Error during creditcard modification");
        }
    } // end method

    // ========================================
    // CREDITCARD — LOGIQUE METIER
    // ========================================

    /**
     * Verifie si la carte de credit doit etre creee ou mise a jour.
     * Si la carte n'existe pas, la cree. Si elle existe et a change, la met a jour.
     *
     * @param newCard la carte saisie par l'utilisateur
     * @param player  le joueur concerne
     * @return true si une creation ou modification a ete effectuee
     * @throws java.sql.SQLException
     */
    public boolean needsUpdate(Creditcard newCard, Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - creditcard input = " + newCard);
        try {
            Creditcard existing = readCreditcardService.read(player);

            // Pas de carte existante → creation
            if (existing == null) {
                LOG.debug(methodName + " - first creditcard for player " + player.getPlayerLastName());
                boolean created = createCreditcardService.create(newCard);
                if (created) {
                    LOG.debug(methodName + " - creditcard created in DB");
                } else {
                    LOG.error(methodName + " - creditcard NOT created in DB");
                }
                return created;
            } // end first cc

            // Carte existante — comparer les champs
            if (existing.getCreditCardExpirationDateLdt().equals(newCard.getCreditCardExpirationDateLdt())
                    && existing.getCreditcardNumber().equals(newCard.getCreditcardNumber())
                    && existing.getCreditcardHolder().equals(newCard.getCreditcardHolder())
                    && existing.getCreditcardVerificationCode().equals(newCard.getCreditcardVerificationCode())
                    && existing.getCreditcardType().equals(newCard.getCreditcardType())) {
                LOG.debug(methodName + " - creditcard unchanged, no update needed");
                return false;
            } else {
                LOG.debug(methodName + " - creditcard modified, updating DB");
                boolean modified = modifyCreditcardService.modify(newCard);
                if (modified) {
                    LOG.debug(methodName + " - creditcard modified in DB");
                } else {
                    LOG.error(methodName + " - creditcard NOT modified in DB");
                }
                return modified;
            } // end equals

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ========================================
    // SAVE RESULT
    // ========================================

    public static class SaveResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private final boolean success;
        private final String message;

        private SaveResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        } // end constructor

        public static SaveResult success(String message) {
            return new SaveResult(true, message);
        } // end method

        public static SaveResult failure(String message) {
            return new SaveResult(false, message);
        } // end method

        public boolean isSuccess() {
            return success;
        } // end method

        public String getMessage() {
            return message;
        } // end method

        @Override
        public String toString() {
            return String.format("SaveResult{success=%s, message='%s'}", success, message);
        } // end method
    } // end class SaveResult

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // tests locaux
    } // end main
    */

} // end class
