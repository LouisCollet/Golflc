package payment;

import entite.Club;
import entite.Cotisation;
import entite.Course;
import entite.Creditcard;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.SQLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentCotisationController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private create.CreateInscription createInscriptionService;
    @Inject private create.CreatePaymentCotisation createPaymentCotisationService;

    public PaymentCotisationController() { }

public boolean RegisterPaymentandInscription(final Creditcard creditcard, final Cotisation cotisation, final Player player, final Round round, final Club club,
        final Course course,
        Inscription inscription) throws SQLException, Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{
           LOG.debug("cotisation={}", cotisation);

  // 1. Register payment  faut le faire avant car l'inscription va vérifier !!!
          if(! payment(cotisation)){
               String msg = "Create Payment Cotisation FAILED — " + cotisation;
               LOG.error(msg);
               throw new Exception(msg);
          }
             LOG.debug("payment registered");
            if(cotisation.getType().equalsIgnoreCase("spontaneous")){
                 LOG.debug("spontaneous payment accepted - no inscription");
                 return true;
            }
               LOG.debug("inscription error={}", inscription.isInscriptionError());
               LOG.debug("inscription ok={}", inscription.isInscriptionOK());
              inscription = createInscriptionService.create(round, player, player,
                      inscription,
                      club, course, "A"); // migrated 2026-02-25
              if( ! inscription.isInscriptionError()){
                  LOG.info("inscription done");
                  return true;
              }else{
                  String msg = "FATAL error : inscription failed BUT payment registered — refund needed";
                  LOG.error(msg);
                  throw new Exception(msg);
              }
}catch (Exception e) {
            LOG.error("Exception in RegisterPaymentandInscription = {}", e.getMessage());
            throw e; // rethrow so REST can propagate real cause to the user via PaymentStateStore
   }
} //end method


private boolean payment(Cotisation cotisation) throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    LOG.debug("cotisation = {}", cotisation);

    if (createPaymentCotisationService.create(cotisation)) { // migrated 2026-02-26
        LOG.info("payments_cotisation created for club={}", cotisation.getIdclub());
        return true;
    }
    LOG.error("payments_cotisation creation failed — cotisation={}", cotisation);
    return false;
} //end method createPaymentCotisation

} // end class
