package create;

import entite.Club;
import entite.Cotisation;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.ValidationsLC;
import entite.ValidationsLC.ValidationStatus;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageInfo;

public class CreateInscription implements interfaces.Log, interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public Inscription create(final Round round,
        final Player player,
        Player invitedBy,     
        final Inscription inscription,
        final Club club,
        final Course course,
        final String batch,
        final Connection conn)
       throws SQLException, InstantiationException{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
   PreparedStatement ps = null;
 try {

   // modification de détail le 26-032020 vérifier à la première exécution
   // utiliser le style de validations de CreateUnavailablePeriod ??
    LOG.debug("... entering " + methodName);
    LOG.debug("CreateInscription - round = " + round);
    LOG.debug("CreateInscription - player = " + player);
//    LOG.debug("CreateInscription - club = " + club);
//    LOG.debug("CreateInscription - course = " + course);
    LOG.debug("CreateInscription - inscription = " + inscription);
 //   LOG.debug("line 000 ?");
 
    inscription.setPlayer_idplayer(player.getIdplayer());
    inscription.setRound_idround(round.getIdround());
       LOG.debug("CreateInscription - inscription completed = " + inscription);
// validations 
     ValidationsLC vlc = new create.CreateInscription().validate(round, player,inscription, club,course, conn);
        LOG.debug("returned to createInscription from validationsLC = "+ vlc.toString());
 // new 12-11-2021
     //   if(vlc.getStatus0().equals(ValidationsLC.ValidationStatus.REJECTED.toString())
     //           && vlc.getStatus2().equals("04")){ 
        if(vlc.getStatus2().equals("04")){   // inscription déjà faite
             LOG.debug("we have error 04");
           inscription.setInscriptionError(true);
           inscription.setErrorStatus(vlc.getStatus2());
           String msg = vlc.getStatus1();
           LOG.error(msg);
           LCUtil.showMessageInfo(msg);
           return inscription;
        }
        
       if(vlc.getStatus0().equals(ValidationsLC.ValidationStatus.REJECTED.toString())){
    //       String msg =  LCUtil.prepareMessageBean("inscription.refused") + club.getClubName() + " ? " ; //+ player.getPlayerLastName());
           String msg = vlc.getStatus1();
           LOG.error(msg);
           LCUtil.showMessageFatal(msg); 
           inscription.setInscriptionError(true);
           inscription.setWeather(vlc.getStatus1()); // astuce provisoire
           inscription.setErrorStatus(vlc.getStatus2());
           return inscription;
        }
        if(vlc.getStatus0().equals(ValidationsLC.ValidationStatus.APPROVED.toString())){
            String msg = "validation Inscription APPROVED = " + vlc.getStatus1();
            LOG.debug(msg);
            LCUtil.showMessageInfo(vlc.getStatus1()); 
            inscription.setInscriptionError(false);  // no error , continue !
        }

       final String query = LCUtil.generateInsertQuery(conn, "player_has_round");
       ps = conn.prepareStatement(query);
       ps.setNull(1, java.sql.Types.INTEGER); // new 07-10-2021 pas certain que cela serve à quelque chose !!
       ps.setInt(2, round.getIdround());
       ps.setInt(3, player.getIdplayer());
       ps.setInt(4, 0);  // Final Results : initial value at zero
       ps.setString(5, inscription.getInscriptionMatchplayTeam()); // new 20-09-2021
       ps.setInt(6, 0);  // NotUsed2
       ps.setString(7, inscription.getInscriptionTeeStart());  //exemple YELLOW / M / 01-18 / 102
       String s = inscription.getInscriptionTeeStart();
        LOG.debug(" field inscriptionTeeStart = " + s);   // BLUE / L / 01-09 / 154
       int tee = Integer.parseInt(s.substring(s.lastIndexOf("/")+2,s.length()));// 2 pos après dernier / jusque fin de string
  //             LOG.debug("tee extracted from inscriptionTeeStart = " + tee);
       inscription.setInscriptionIdTee(tee);
       ps.setInt(8, inscription.getInscriptionIdTee());
       ps.setInt(9, invitedBy.getIdplayer());
       ps.setTimestamp(10, Timestamp.from(Instant.now()));
       utils.LCUtil.logps(ps);
       int row = ps.executeUpdate(); // write into database
       if(row == 1) {  // l'inscription est réussie
           LOG.debug("InscriptionId created = " + LCUtil.generatedKey(conn));
           String msg =  LCUtil.prepareMessageBean("inscription.ok") + " = " + inscription;
           LOG.debug(msg);
           showMessageInfo(msg);
           if(batch.equalsIgnoreCase("A")){  // pas de mail si Batch ou inscription other players ?
                if(new mail.InscriptionMail().create(player, invitedBy, round, club, course)){
                    LOG.debug("result YES send mail = ");
                }else{
                    LOG.debug("result NOT send mail = ");
                }
                    
            }
           inscription.setInscriptionError(false);
           inscription.setErrorStatus("00");
           return inscription;
       }else{
            String msg = "-- NOT NOT successful Insert in create Inscription !!! " + row;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            inscription.setInscriptionError(true);
            inscription.setErrorStatus("90");
            return inscription;
            }
    } catch (SQLException sqle) {
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 ){
                 String msg = LCUtil.prepareMessageBean("create.inscription.duplicate")
                         + " round = " + round.getIdround()
                         + " player = " + player.getIdplayer()
                         + " player name = " + player.getPlayerLastName();
                 LOG.error(msg);
                 LCUtil.showMessageFatal(msg);
                 inscription.setInscriptionOK(true); // new 10/7/2017 pour permettre inscription other players in inscription.xhtml
          //       return 98;//null;
                 inscription.setInscriptionError(true);
                 inscription.setErrorStatus("98");
                 return inscription;
            }else{
                 String msg = "SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
                 LOG.error(msg);
                 LCUtil.showMessageFatal(msg);
                 inscription.setInscriptionError(true);
                 inscription.setErrorStatus("99");
                 return inscription;
            //    return 98;//null;
            }
    }catch (Exception e){
            String msg = "£££ Exception in createInscription = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            inscription.setInscriptionError(true);
            inscription.setErrorStatus("998");
            return inscription;
        } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }

    } //end method


public ValidationsLC validate(final Round round, final Player player, final Inscription inscription,
        final Club club, final Course course, final Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
     ValidationsLC v = new ValidationsLC();
     v.setStatus0(ValidationStatus.APPROVED.toString());
     v.setStatus1("");
     v.setStatus2("00");

   try{
       LOG.debug(" ... entering " + methodName + " before create inscription");
       
    /*/ enlevé 03-01-2023 ne fonctoionne plus  
       new 19/06/2022 vérifier si le course est Unavailable pour un round
        Structure str = new UnavailableController().isRoundUnavailable(club, round, conn);
         LOG.debug("inscription validation - " + str);
           if(str.getStatus()){ // true : le course est unavailable
                v.setStatus0(ValidationStatus.REJECTED.toString());
                String msg =  LCUtil.prepareMessageBean("inscription.unavailable") + str.getItem() + " for " + course.getCourseName();
                v.setStatus1(msg);
                v.setStatus2("05");
                return v;
           }
       */
        List<Player>listPlayers = new lists.RoundPlayersList().list(round, conn);
   //     LOG.debug("line 06");
        if(listPlayers == null){ // cherché longtemps ! 26-06-2020
            LOG.debug("listPlayers is null");
            listPlayers = Collections.<Player>emptyList(); 
            LOG.debug("emptyList size is " + listPlayers.size());
        }else{
    //        LOG.debug(" ... listPlayers is not null = " + listPlayers.toString()); 
    LOG.debug("number players already inscripted = " + listPlayers.size());
             listPlayers.forEach(item -> LOG.debug("from lists.RoundPlayersList(= " + item.getIdplayer() + " / " + item.getPlayerLastName())); 
        }     
        
   //     if(listPlayers.contains(player)){
    //        LOG.debug("listPlayers already contains " + player);
    //    }
        
       /*
        if(listPlayers.size() == 0){     
                v.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
                String msgerr =  LCUtil.prepareMessageBean("inscription.noplayers");
                LOG.debug(msgerr);
                LOG.debug("line 01");
       //         v.setStatus1(msgerr);
       //         v.setStatus2("01");
       //         return v;
           }
    */

  //      LOG.debug("line 07");
  if(listPlayers.size() > 0){
      LOG.debug("validation - from listPlayers : there are already {} players for this round ! ", listPlayers.size());
      LOG.debug("validation - from listPlayers : here are their names : ", Arrays.toString(listPlayers.toArray()));
//      LOG.debug("validation - from Round their names are : " + round.getPlayersList());
  }
        
        
   // vérifier si pas 5e inscription ??
//        LOG.debug("roundPlayers =" + round.getPlayers());
        LOG.debug("roundPlayersString =" + round.getPlayersString());
        LOG.debug("roundGame=" + round.getRoundGame());
        if(listPlayers.size() > 3 ){ 
                v.setStatus0(ValidationStatus.REJECTED.toString());
                String msgerr =  LCUtil.prepareMessageBean("inscription.toomuchplayers" + listPlayers.size());
                v.setStatus1(msgerr);
                v.setStatus2("01");
                return v;
           }
        
        LOG.debug("idplayer = " + player.getIdplayer());
        LOG.debug("club = " + club.getIdclub());
        LOG.debug("round = " + round.getRoundDate());

        if(player.getPlayerRole().equals("ADMIN")){ // administateur LC){ 
                v.setStatus0(ValidationStatus.APPROVED.toString());
                String msg =  LCUtil.prepareMessageBean("inscription.administrator");
                v.setStatus1(msg);
                v.setStatus2("00");
                return v;
           }
// new 12-11-2021
        if(new find.FindInscriptionRound().find(round, player, conn)){  // est déjà inscrit
                v.setStatus0(ValidationStatus.REJECTED.toString());
                String msg =  LCUtil.prepareMessageBean("inscription.duplicate");
                v.setStatus1("inscription.duplicate");
                v.setStatus2("04");
                return v;
           }

            Cotisation cotisation = new find.FindCotisationAtRoundDate().find(player,club,round,conn);
   //             LOG.debug("in validation CreateInscription,Cotisation = " + cotisation.toString());
                LOG.debug("cotisation at round date = " + cotisation);
                LOG.debug("cotisation Status = " + cotisation.getStatus());
        
             if(new find.FindGreenfeePaid().find(player,round,conn)){ // le greenfee est déjà payé
   //             || cotisation.getStatus().equalsIgnoreCase("y")){  // was = y
                v.setStatus0(ValidationStatus.APPROVED.toString());
                String msg =  LCUtil.prepareMessageBean("inscription.greenfee");
                v.setStatus1(msg);
      //          v.setStatus2("00");
                return v;
            }   
 
 //           if(cotisation.getStatus() == null){ // pas de cotisation existante
 //               v.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
 ////               String msg = "Il n'y a pas de cotisation à cette date : " + round.getRoundDate();
 //               v.setStatus1(msg);
  //              v.setStatus2("02");
  //              return v;
 //           }  
        
            
          if(cotisation.getStatus().equals("Y")){ 
                v.setStatus0(ValidationStatus.APPROVED.toString());
                String msg =  LCUtil.prepareMessageBean("inscription.member");
                v.setStatus1(msg);
                v.setStatus2("00");
                return v;
            }
          
            if(cotisation.getStatus().equals("NF")
                && (! new find.FindGreenfeePaid().find(player,round,conn))){ 
                v.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
                String msg = "Cotisation pas trouvée, greenfee pas trouvé";
         //       LOG.debug(msg);
                v.setStatus1(msg);
                v.setStatus2("02");
                return v;
            }

            if(cotisation.getStatus().equals("N")){ 
                v.setStatus0(ValidationStatus.REJECTED.toString());
                 String msg = "cotisation.notmember";
                v.setStatus1(msg);
                v.setStatus2("03");
                return v;
            }

     return v;
  } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + " / " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            v.setStatus0(ValidationStatus.REJECTED.toString());
            return v;
 } finally {
        }     
// return v;
} // end method validate2
/*
public Integer validate(final Round round, final Player player, final Inscription inscription,
        final Club club, final Course course, final Connection conn) throws SQLException{
 //  PreparedStatement ps = null;
   try{
       LOG.debug("entering validation before create inscription");
        List<Player> listPlayers = new lists.RoundPlayersList().list(round, conn);
        LOG.debug("there are already {} players for this round ! ", listPlayers.size());
        LOG.debug("here are their names : ", Arrays.toString(listPlayers.toArray()));
        LOG.debug("there are RoundPlayers for this round : " + round.getRoundPlayers());
        LOG.debug("here are their names : ", round.getPlayersList()); //.toString); //(listPlayers.toArray()));
        
      if(listPlayers.size() > 4){  // maximum 4 players par flight !
          return 01;
      }
        LOG.debug("current player = " + player.getIdplayer());
        if(player.getPlayerRole().equals("ADMIN")){ // administateur LC
            LOG.debug("current player is ADMIN");
            return 00;
        }
  //      if( ! player.getPlayerRole().equals("ADMIN")){ // administateur LC
  //        LOG.debug("test cotisation for non LC");
          Cotisation cotisation = new find.FindCotisation().find(player,club,round,conn);
            LOG.debug("in createinscription,Cotisation = " + cotisation.toString());
            LOG.debug("cotisation Status = " + cotisation.getStatus());
          if(cotisation.getStatus().equals("Y")){ 
                String msg = "Vous êtes membre de ce club vous pouvez donc vous inscrire !";
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return 00;
            }
    // new 20-02-2019
            if(new find.FindGreenfeePaid().find(player,round,conn)){ 
                String msg = "Inscription : Vous avez payé votre greenfee !";
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return 00;
            }

            if(cotisation.getStatus().equals("NF")){ 
                // cotisation pas trouvée
               return 02;
            }

            if(cotisation.getStatus().equals("N")){ 
                // cotisation mais pas membre
               return 03;
            }

            if(cotisation.getStatus() == null){
            // quel cas ?
            return 04;
            }

         } catch (Exception e) {
            String msg = "£££ Exception in validate Inscription = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return 99;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
    //        DBConnection.closeQuietly(null, null, null, null); // new 14/08/2014
        }     
 return 99;
} // end method validate
*/

    void main() throws SQLException, Exception{ //enlevé static
        LOG.debug("line 01");

    LOG.debug("line 03, map = "); // + get);
      Connection conn = new DBConnection().getConnection();
  try{
   Player player = new Player();
   player.setIdplayer(324714);
   // load
   player.setPlayerRole("ADMIN");
   Player invitedBy = player;
   Round round = new Round(); 
   round.setIdround(435);
   Club club = new Club();
   club.setIdclub(1135);
   Course course = new Course();
   course.setIdcourse(135);
   Inscription inscription = new Inscription();
   inscription.setInscriptionIdTee(154);
   String batch = "A";
 //   int lp = new CreateInscription().create(round, player, invitedBy, inscription, club, course, batch, conn);
    var lp = new CreateInscription().create(round, player, invitedBy, inscription, club, course, batch, conn);
    LOG.debug("from main, after lp = " + lp);
        
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main

}  //en class