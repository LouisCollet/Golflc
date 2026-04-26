package payment;

// import Controllers.CreditcardController; // removed 2026-02-25
// import Controllers.TarifGreenfeeController; // removed 2026-02-25
import entite.Club;
import entite.Course;
import entite.Creditcard;
import entite.Greenfee;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.TarifGreenfee;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import java.io.*;
// import java.sql.Connection; // removed 2026-02-25
import java.sql.SQLException;
import java.util.Arrays;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentGreenfeeController implements Serializable, interfaces.Log{

    private static final long serialVersionUID = 1L;

    @Inject private create.CreateInscription      createInscriptionService;      // migrated 2026-02-25
    @Inject private create.CreatePaymentGreenfee  createPaymentGreenfeeService;  // migrated 2026-02-25
    @Inject private manager.RoundManager          roundManager;                  // added 2026-04-19 flight booking
    @Inject private find.FindRoundBySlot          findRoundBySlot;               // find-or-create au slot — 2026-04-21
    @Inject private find.FindGreenfeePaid         findGreenfeePaid;              // skip insert si déjà payé — 2026-04-21
    @Inject private find.FindInscriptionRound     findInscriptionRound;          // idempotence auto-inscription — 2026-04-21
    @Inject private find.FindTeeStart             findTeeStart;                  // 1er tee valide — 2026-04-21
    @Inject private lists.ParticipantsRoundList   participantsRoundList;         // max 4 joueurs — 2026-04-21

    private static final int MAX_PLAYERS_PER_SLOT = 4;

public PaymentGreenfeeController(){ // constructor
    //
}
// new 21-01-2023 vient de coursecontroller
public boolean RegisterPaymentandInscription(final Creditcard creditcard, final Greenfee greenfee, final Player player, final Round round, final Club club,
        final Course course,
        Inscription inscription) throws SQLException, Exception { // conn removed 2026-02-25
try{
           LOG.debug("entering RegisterPaymentandInscriptionGreenfee");
           LOG.debug("with greenfee = " + greenfee);

  // 0. Find-or-create du round au slot — robustesse concurrence + reliquats en DB
           if (round.getIdround() == null) {
               Round existing = findRoundBySlot.find(course.getIdcourse(), round.getRoundDate(),
                       java.time.ZoneId.of(club.getAddress().getZoneId()));
               if (existing != null) {
                   round.setIdround(existing.getIdround());
                   LOG.debug("round found by slot — reusing idround={}", existing.getIdround());
               } else {
                   LOG.debug("round.idround null and no round at slot — creating");
                   manager.RoundManager.SaveResult result =
                       roundManager.createRound(round, course, club, new entite.UnavailablePeriod());
                   if (!result.isSuccess()) {
                       String msg = "Round creation failed before greenfee payment: " + result.getMessage();
                       LOG.error(msg);
                       showMessageFatal(msg);
                       throw new Exception(msg);
                   }
                   LOG.debug("round created idround={}", round.getIdround());
               }
               greenfee.setIdround(round.getIdround());  // sync greenfee avec l'idround (existant ou nouveau)
           }

  // 1. Register payment (skip si déjà payé pour ce (player, round) — reliquat d'un essai précédent)
           if (findGreenfeePaid.find(player, round)) {
               LOG.info("PaymentGreenfee already registered for player={} round={} — skipping insert",
                       player.getIdplayer(), round.getIdround());
           } else if(! payment(player, greenfee)){
                  String msg = "Create Payment Greenfee FAILED - no round inscription accepted !!";
                  LOG.error(msg);
                  showMessageFatal(msg);
                  throw new Exception(msg);
           } else {
               LOG.debug("PaymentGreenfee registered");
           }

  // 2. Auto-inscription (idempotent) — 1er tee valide via FindTeeStart
           if (findInscriptionRound.find(round, player)) {
               LOG.debug("player {} déjà inscrit — skip auto-inscription", player.getIdplayer());
               return true;
           }
           // Filet de sécurité max 4 joueurs — race condition avec un autre payeur concurrent
           participantsRoundList.invalidateCache();
           int inscrits = participantsRoundList.list(round).size();
           if (inscrits >= MAX_PLAYERS_PER_SLOT) {
               String msg = "Créneau complet (" + inscrits + "/" + MAX_PLAYERS_PER_SLOT
                       + ") — inscription refusée après paiement. Contacter l'administrateur pour un remboursement.";
               LOG.error(msg);
               showMessageFatal(msg);
               return true;   // paiement enregistré, mais inscription non créée
           }
           findTeeStart.invalidateCache();
           java.util.List<String> teeStarts = findTeeStart.find(course, player, round);
           if (teeStarts == null || teeStarts.isEmpty() || !teeStarts.get(0).contains("/")) {
               LOG.warn("no tee found for gender {} — inscription deferred to Register Score", player.getPlayerGender());
               return true;
           }
           Inscription auto = new Inscription();
           auto.setInscriptionTeeStart(teeStarts.get(0));
           LOG.debug("auto-inscription with tee = {}", teeStarts.get(0));
           Inscription result = createInscriptionService.create(round, player, player, auto, club, course, "A");
           if (result == null || result.isInscriptionError()) {
               LOG.warn("GREENFEE auto-inscription failed — user can retry via Register Score");
           } else {
               LOG.info("GREENFEE auto-inscription created player={} round={}", player.getIdplayer(), round.getIdround());
           }
           return true;
}catch (Exception e) {
            String msg = "££ Exception in RegisterPaymentandInscriptionGreenfee  = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
           return false; // indicates that the same view should be redisplayed
     //   } finally {}
//return null;
   } 
} //end method

/* legacy — not called by registrars — uses new TarifGreenfeeController() and new CreditcardController()
public String manageGreenfee(TarifGreenfee tarifGreenfee, Club club, Round round, Player player) throws Exception{
    // commented out 2026-02-25
} //end method manageGreenfee
*/





private boolean payment(Player player, Greenfee greenfee) { // static removed + conn removed 2026-02-25
try{
        LOG.debug("entering payment");
        LOG.debug("with greenfee = " + greenfee);
        LOG.debug("droppedPlayers is before : " + Arrays.toString(player.getDroppedPlayers().toArray()));
        int size = player.getDroppedPlayers().size();
        LOG.debug("size/number of iterations players = " + size );
        if(size != 0){
   //         Player p = new Player();
            for(int i=0; i < size; i++){
    //        LOG.debug(" -- treated idplayer = " + player.getDroppedPlayers().get(i).getIdplayer() );
                Player p = player.getDroppedPlayers().get(i);
                   LOG.debug("we have to CreateGreenfee for :" + p.toString());
                // boolean b = new create.CreatePaymentGreenfee().create(p, greenfee);
                boolean b = createPaymentGreenfeeService.create(p, greenfee); // migrated 2026-02-25
                   LOG.debug("create other players OK");
            } //end for

        } //end if

      // if(new create.CreatePaymentGreenfee().create(player, greenfee)){ // true
      if(createPaymentGreenfeeService.create(player, greenfee)){ // migrated 2026-02-25
  //        LOG.debug("after createGreenfee : we are OK");
          String msg = prepareMessageBean("greenfee.success") 
                + greenfee.getRoundDate().format(ZDF_DAY) + " - " 
                + " Round = " + greenfee.getIdround();
          LOG.info(msg);
          showMessageInfo(msg);
      return true;
     }else{
        String msg = "Error :Greenfee NOT paid !!";
        LOG.error(msg);
 //       showMessageFatal(msg);
        return false; 
    }
 }catch (SQLException e){
            String msg = "SQL Exception in PaymentsGreenfeeController = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }catch (Exception ex){
            String msg = "Exception in PaymentsGreenfeeController " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
 } //end method createPaymentGreenfee



    /*
    void main() throws Exception, Throwable {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
/*
public boolean createPaymentCotisation(Player player, Cotisation cotisation, Connection conn){ 
 try{
      LOG.debug("entering createPaymentCotisation");
      LOG.debug("with cotisation = " + cotisation);
 
            
      if(new create.CreatePaymentCotisation().create(cotisation, conn)){ //true
        String msg = LCUtil.prepareMessageBean("cotisation.success")
                  + cotisation //.getCotisationStartDate().format(ZDF_DAY) + " - " 
          //        + cotisation.getCotisationEndDate().format(ZDF_DAY)
                  + " for club = " + cotisation.getIdclub();
        LOG.info(msg);
        showMessageInfo(msg);
        return true;
     }else{
        String msg = "Error : cotisation NOT modified !!";
        LOG.error(msg);
        showMessageFatal(msg);
        return false; // retourne d'ou il vient : où ??
    }
  }catch (Exception ex){
            String msg = "Exception in createCotisation " + ex.getLocalizedMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
 } //end method createPaymentCotisation
*/
