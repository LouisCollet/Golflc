package create;

import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class CreateInscriptionOtherPlayers implements interfaces.Log, interfaces.GolfInterface{

public boolean create(final Player player, final Round round, final Inscription inscription,
        final Club club, final Course course, final Connection conn) throws SQLException{
  try{
     LOG.info("entering create.CreateInscriptionOtherPlayers");
  //   LOG.info("DroppedPlayers = " + player.getDroppedPlayers().toString());
     LOG.info("inscription = " + inscription.toString());
     LOG.info("round = " + round.toString());
     LOG.info("list Dropped players = " + Arrays.toString(player.getDroppedPlayers().toArray() ) );
////  player.getDroppedPlayers().forEach(item -> LOG.info("Liste DroppedPlayers =  " + item + "/"));
 //    LOG.info(" round PlayersString = " + round.getPlayersString());
      // LOG.info(" round Liste getPlayers liste other = " + round.getPlayers());
////  round.getPlayers().forEach(item -> LOG.info("from round : Liste getPlayers " + item + "/")); // java 8 lambda
  
  //   LOG.info("inscription = " + inscription.toString());
  //   LOG.info("club = " + club.toString());
  //   LOG.info("course = " + course.toString());
  int size = player.getDroppedPlayers().size();
  LOG.info("number of iterations players = " + size );
   Player p = new Player();

   for(int i=0; i < size; i++){
    //        LOG.info(" -- treated idplayer = " + player.getDroppedPlayers().get(i).getIdplayer() );
        p = player.getDroppedPlayers().get(i);
            LOG.info("traitement de l'inscription pour :" + p.toString());
        int OK = new create.CreateInscription().create(round, p, player, inscription, club, course, conn);
        if(OK != 0){
            String msg = "Inscription other players NOT OK for player = " + p.getIdplayer() + " / " + p.getPlayerLastName();
            LOG.info(msg);
            showMessageInfo(msg);
            
        }else{
            //          LOG.info("roundPlayers is now = " + round.getRoundPlayers());
         //    String s = utils.LCUtil.fillRoundPlayersString(player.getDroppedPlayers());  //les nouveaux, pas les anciens
        //    String s = utils.LCUtil.fillRoundPlayersString(round.getPlayersList()); //player.getDroppedPlayers()); 
        // before add current player    
             String s = round.getPlayersString();
           LOG.info("Joueurs inscrits précédemment = " + s);
        // add now current player
            ArrayList<Player> ap = new ArrayList<>();   // transforme player2 in list<player<    
            ap.add(p);
            String s1 = utils.LCUtil.fillRoundPlayersString(ap);  // this function needs a List !
            round.setPlayersString(s + "," + s1);
                LOG.info("PlayersString is now " + round.getPlayersString());
            // mettre à jour le compteur de players
                LOG.info("RoundPlayers was " +  round.getRoundPlayers());
            short sh8 = round.getRoundPlayers();
            round.setRoundPlayers(++sh8);
                LOG.info("RoundPlayers is now " +  round.getRoundPlayers());
  //              LOG.info("supprimer " + player2.getIdplayer() + " de la liste DroppedPlayers");
            player.getDroppedPlayers().remove(i); // ne sert à rien : travaille sur la version locale !!
   //             LOG.info("list dropped players after remove = " + Arrays.toString(player.getDroppedPlayers().toArray() ) );
        } // end else cas normal : inscription OK
     }  // end for
  
    LOG.info("exiting create.CreateInscriptionOtherPlayers");
   return true;
   }catch(Exception ex){
    String msg = "create.CreateInscriptionOtherPlayers Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
    }
}  // end method
}  //end class