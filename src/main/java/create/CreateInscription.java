package create;

import entite.Club;
import entite.Cotisation;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class CreateInscription implements interfaces.Log, interfaces.GolfInterface{
public Integer create(final Round round, final Player player, Player invitedBy,
   final Inscription inscription, final Club club, final Course course, final Connection conn) throws SQLException{
    PreparedStatement ps = null;
   try {
       int retour = new CreateInscription().validate(round, player,inscription, club,course, conn);
       if(retour != 00){
           LOG.info("there is a validation error");
           return retour;
       }
    LOG.info("entering CreateInscription-create");
    LOG.info("CreateInscription - round = " + round.toString());
    LOG.info("CreateInscription - player = " + player.toString());
    LOG.info("CreateInscription - club = " + club.toString());
    LOG.info("CreateInscription - course = " + course.toString());
    LOG.info("CreateInscription - inscription = " + inscription.toString());
    
       final String query = LCUtil.generateInsertQuery(conn, "player_has_round");
 //           LOG.info("generated query = " + query);
       ps = conn.prepareStatement(query);
  //          LOG.info("line 01");
       ps.setInt(1, round.getIdround());
  //           LOG.info("line 02");
            ps.setInt(2, player.getIdplayer());
  //           LOG.info("line 03");
            ps.setInt(3, 0);  // Final Results : initial value at zero
            ps.setInt(4, 0);  // Final ZwanzeursResults : initial value at zero
            ps.setInt(5, 0);  // Final ZwanzeursGreenshirt : initial value at zero
     //       ps.setString(6, playerhasround.getInscriptionTeam() );  // new 28/09/2014 // deleted 26/06/2017
            ps.setString(6, inscription.getInscriptionTeeStart());  // new 08/06/2015
   //          LOG.info("line 05");
            String s = inscription.getInscriptionTeeStart();
    //    LOG.info("line 06");
        LOG.info("string s = " + s);
         // BLUE / L / 01-09 / 154
            String s3 = s.substring(s.lastIndexOf("/")+2,s.length() ); // 2 pos après dernier / jusque fin de string
            LOG.info("string s3 = " + s3);
            inscription.setInscriptionIdTee(Integer.valueOf(s3));
            ps.setInt(7, inscription.getInscriptionIdTee());  // new 31/03/2019
   //          LOG.info("line 06");
            ps.setInt(8, invitedBy.getIdplayer());  // new 14/02/2018
  //           LOG.info("line 07");
            ps.setTimestamp(9, LCUtil.getCurrentTimeStamp());
                utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
  //           LOG.info("line 01");
            if(row == 1) {  // l'inscription est réussie
             String msg =  LCUtil.prepareMessageBean("inscription.ok");
              msg = msg + "for round = " + round.getIdround()
                      + " <br/> player = " + player.getIdplayer()
                      + " <br/> player name = " + player.getPlayerLastName()
                      + " <br/> round date = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                      + " <br/> idtee = " + inscription.getInscriptionIdTee()
                ;
               LOG.info(msg);
           //    LCUtil.showMessageInfo(msg);
     return 00;  // success
            }else{
                String msg = "-- NOT NOT successful Insert in create Inscription !!! " + row;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return 90;  // null
            }
        } catch (SQLException sqle) {
         //   String msg = "";
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 ){
                 String msg = LCUtil.prepareMessageBean("create.inscription.duplicate");
                 msg = msg + player.getIdplayer();
                 msg = msg + " round = " + round.getIdround();
                 LCUtil.showMessageFatal(msg);
                 inscription.setInscriptionOK(true); // new 10/7/2017 pour permettre inscription other players in inscription.xhtml
                 return 98;//null;
            }else{
                 String msg = "SQLException in createInscription = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
                 LOG.error(msg);
                 LCUtil.showMessageFatal(msg);
                return 98;//null;
            }
        } catch (Exception e) {
            String msg = "£££ Exception in createInscription = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return 99; //null;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//   return 95;
    } //end method

public Integer validate(final Round round, final Player player, final Inscription inscription,
        final Club club, final Course course, final Connection conn) throws SQLException{
    PreparedStatement ps = null;
   try{
       LOG.info("entering validation before create inscription");
        List<Player> listPlayers = new lists.RoundPlayersList().list(round, conn);
        LOG.info("there are already {} players for this round ! ", listPlayers.size());
        LOG.info("here are their names : ", Arrays.toString(listPlayers.toArray()));
        LOG.info("there are RoundPlayers for this round : " + round.getRoundPlayers());
        LOG.info("here are their names : ", round.getPlayersList()); //.toString); //(listPlayers.toArray()));
        
      if(listPlayers.size() > 4){  // maximum 4 players par flight !
          return 01;
      }
        LOG.info("current player = " + player.getIdplayer());
        if(player.getPlayerRole().equals("ADMIN")){ // administateur LC
            LOG.info("current player is ADMIN");
            return 00;
        }
  //      if( ! player.getPlayerRole().equals("ADMIN")){ // administateur LC
  //        LOG.info("test cotisation for non LC");
          Cotisation cotisation = new find.FindCotisation().find(player,club,round,conn);
            LOG.info("in createinscription,Cotisation = " + cotisation.toString());
            LOG.info("cotisation Status = " + cotisation.getStatus());
          if(cotisation.getStatus().equals("Y")){ 
                String msg = "Vous êtes membre de ce club vous pouvez donc vous inscrire !";
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return 00;
            }
    // new 20-02-2019
            if(new find.FindGreenfeePaid().find(player,club,round,conn)){ 
                String msg = "Inscription : Vous avez payé votre greenfee !";
                LOG.info(msg);
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
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }     
 return 99;

} // end method validate


    public static void main(String[] args) throws SQLException, Exception{ //enlevé static
        LOG.info("line 01");

    LOG.info("line 03, map = "); // + get);
      Connection conn = new DBConnection().getConnection();
  try{
   Player player = new Player();
   player.setIdplayer(324714);
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
 
    int lp = new CreateInscription().create(round, player, invitedBy, inscription, club, course, conn);
        LOG.info("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main

}  //en class