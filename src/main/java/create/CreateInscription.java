package create;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.PlayerHasRound;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class CreateInscription implements interfaces.Log, interfaces.GolfInterface
{
public boolean createInscription(final Round round, final Player player, Player invitedBy,
   final PlayerHasRound inscription, final Club club, final Course course, final Connection conn) throws SQLException
    {
    PreparedStatement ps = null;
   try {
        List<Player> listPlayers = null;
       
            LOG.debug(" ... starting createInscription()... ");
            LOG.info("round ID      = " + round.getIdround());
            LOG.info("Player ID     = " + player.getIdplayer());
            LOG.info("Invited By     = " + invitedBy.getIdplayer());
            LOG.info("Player Email  = " + player.getPlayerEmail());
            
  //          LOG.info("Player Gender = " + player.getPlayerGender() ); // new 19/08/2014
            if(round.getRoundDate() != null){
              LOG.info("Round Date = " + round.getRoundDate().format(ZDF_TIME_HHmm));
              LOG.info("Round Game    = " + round.getRoundGame()); 
            }
            
              LOG.info("Tee Start = " + inscription.getInscriptionTeeStart() );
//validation 1 supprimé 25/06/2017
  //    if(playerhasround.getInscriptionTeam().equals("") && round.getRoundGame().equals(Round.GameType.SCRAMBLE.toString()) )
  //         { String msgerr = "Error inscription : Team must be completed for Scramble !! ";
  //              throw new LCCustomException(msgerr);
  //         }
//validation 2
      lists.RoundPlayersList spl = new lists.RoundPlayersList();
      listPlayers = spl.listAllParticipants(round, conn);
        LOG.info("there are already {} players for this round ! ", listPlayers.size());
        LOG.info("there are RoundPlayers for this round : " + round.getRoundPlayers());
      if(listPlayers.size() > 4){  // maximum 4 players par flight !
          String msgerr =  LCUtil.prepareMessageBean("inscription.too much players"); // + listPlayers.size() ;
          LOG.error(msgerr); 
          LCUtil.showMessageFatal(msgerr);
          return false;
          
      }
            final String query = LCUtil.generateInsertQuery(conn, "player_has_round");
            //String query = "INSERT INTO player_has_round VALUES (?,?,?,?)";
            ps = conn.prepareStatement(query);
            ps.setInt(1, round.getIdround());
            ps.setInt(2, player.getIdplayer());
            ps.setInt(3, 0);  // Final Results : initial value at zero
            ps.setInt(4, 0);  // Final ZwanzeursResults : initial value at zero
            ps.setInt(5, 0);  // Final ZwanzeursGreenshirt : initial value at zero
     //       ps.setString(6, playerhasround.getInscriptionTeam() );  // new 28/09/2014 // deleted 26/06/2017
            ps.setString(6, inscription.getInscriptionTeeStart());  // new 08/06/2015
            ps.setInt(7, invitedBy.getIdplayer());  // new 14/02/2018
            ps.setTimestamp(8, LCUtil.getCurrentTimeStamp());
 //      entite.PlayerHasRound.setPlayerGender(player.getPlayerGender() ); // new 19/08/2014 for custm validations
             //    String p = ps.toString();
                utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0) { //int key = LCUtil.generatedKey(conn);
                //  LOG.info("Player Has Round generatedKey = " + key);
//                setNextScorecard(true); // affiche le bouton carte de score ??
                
                String msg =  LCUtil.prepareMessageBean("inscription.ok");
                msg = msg
                        + "for round = " + round.getIdround()
                        + " <br/> player = " + player.getIdplayer()
                        + " <br/> player name = " + player.getPlayerLastName()
                        + " <br/> round date = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                    ;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                
                mail.InscriptionMail im = new mail.InscriptionMail();
                im.sendInscriptionMail(player, invitedBy, round, club, course);
       return true;
            }else{
                String msg = "-- NOT NOT successful Insert in create Inscription !!! " + row;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return false;  // null
            }
        } //end try
        catch (NullPointerException npe) {
            String msg = "£££ NullPointerException in createInscription = " + npe.getMessage()
                    + " player = " + player.getIdplayer() + " round = " + round.getIdround();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false; // null;
//        } catch (MySQLIntegrityConstraintViolationException cv) {
//            String msg = "MySQLIntegrityConstraintViolationException in insert Inscription = " + cv.getMessage();
//            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
 //           return false; //null;
        } catch (SQLException sqle) {
            String msg = "";
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 )
            {
                 msg = "Vous êtes déjà inscrit à ce Tour ! =  player = "
                         + player.getIdplayer() + " round = " + round.getIdround();
                 inscription.setInscriptionOK(true); // new 10/7/2017 pour permettre inscription other players in inscription.xhtml
            }else{
                 msg = "SQLException in createInscription = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();}
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;//null;
        } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in createInscription = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false; //null;
        } catch (Exception e) {
            String msg = "£££ Exception in createInscription = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false; //null;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
    } //end method
}  //en class