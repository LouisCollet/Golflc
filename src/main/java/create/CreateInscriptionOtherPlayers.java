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

public class CreateInscriptionOtherPlayers implements interfaces.Log, interfaces.GolfInterface{

public boolean createInscriptions(final Player player, final Round round, final Inscription inscription,
        final Club club, final Course course, final Connection conn) throws SQLException{
  try{
     LOG.info("entering createInscriptionOtherPlayers");
     LOG.info("DroppedPlayers = " + player.getDroppedPlayers().toString());
 //    LOG.info("round = " + round.toString());
     LOG.info("list dropped players = " + Arrays.toString(player.getDroppedPlayers().toArray() ) );
////  player.getDroppedPlayers().forEach(item -> LOG.info("Liste DroppedPlayers =  " + item + "/"));
 //    LOG.info(" round PlayersString = " + round.getPlayersString());
      // LOG.info(" round Liste getPlayers liste other = " + round.getPlayers());
////  round.getPlayers().forEach(item -> LOG.info("from round : Liste getPlayers " + item + "/")); // java 8 lambda
  
  //   LOG.info("inscription = " + inscription.toString());
  //   LOG.info("club = " + club.toString());
  //   LOG.info("course = " + course.toString());
   LOG.info("number of iterations players = " + player.getDroppedPlayers().size() );
   Player player2 = new Player();
   // le for ne sert à rien : c'est toujours 1
   for(int i=0; i < player.getDroppedPlayers().size() ; i++)
     {
          // fields pour envoyer le mail de confirmation de l'inscription
            LOG.debug(" -- item in for idplayer = " + player.getDroppedPlayers().get(i).getIdplayer() );
            
        player2 = player.getDroppedPlayers().get(i);
        create.CreateInscription ci = new create.CreateInscription();
        boolean OK = ci.createInscription(round, player2, player, inscription, club, course, conn); // new 21/07/2014
        if(OK){
            LOG.info("Inscription created for other player = " + player2.getIdplayer() + " / " + player2.getPlayerLastName());
  //          LOG.info("roundPlayers is now = " + round.getRoundPlayers());
            LOG.info("roundPlayersString was = " + round.getPlayersString());
         //    String s = utils.LCUtil.fillRoundPlayersString(player.getDroppedPlayers());  //les nouveaux, pas les anciens
        //    String s = utils.LCUtil.fillRoundPlayersString(round.getPlayersList()); //player.getDroppedPlayers()); 
        // before add current player    
        String s = round.getPlayersString();
   //            LOG.info("reformated PlayersString = " + s);
        // add now current player
        ArrayList<Player> p = new ArrayList<>();   // transforme player2 in list<player<    
        p.add(player2);
        String s1 = utils.LCUtil.fillRoundPlayersString(p);  // this function needs a List !
        round.setPlayersString(s + "," + s1);
              LOG.info("PlayersString is now " + round.getPlayersString());
            // mettre à jour le compteur de players
             LOG.info("RoundPlayers was " +  round.getRoundPlayers());
        short sh8 = round.getRoundPlayers();
        round.setRoundPlayers(++sh8);
             LOG.info("RoundPlayers is now " +  round.getRoundPlayers());
  //              LOG.info("supprimer " + player2.getIdplayer() + " de la liste DroppedPlayers");
        player.getDroppedPlayers().remove(i);
   //             LOG.info("list dropped players after remove = " + Arrays.toString(player.getDroppedPlayers().toArray() ) );
        return true;
        }else{
          LOG.info( "error creation inscription other players");
          return false;
        }
     }  // end for
  
    LOG.info("exiting createInscriptionOtherPlayers");
   return true;
   }catch(Exception ex){
    String msg = "creatinscriptionother players Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}    
}  // end method

}  //en class