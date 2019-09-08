package lists;

import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class RoundPlayersList implements interfaces.Log{  
//en réalité vaut pour tous games !
    private static List<Player> liste = null;
    
public List<Player> list(final Round round ,final Connection conn) throws SQLException { 
 
if(liste == null)
{    LOG.debug("starting RoundPlayersList "  );
 //    LOG.debug("starting RoundPlayersList for round =  " + round.toString()  );
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
   ///      String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
  //   String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
     String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
     
     
    String query =
      "SELECT " + ph + "," + ro + "," + pl + // ","  + 
  //   " SELECT playerLastName, playerlanguage, playeremail, playercity, InscriptionIdPlayer,"
  //   + " round.RoundGame, round.idround, round.RoundGame,"
  //   + " round.RoundCompetition, round.roundTeam" +
            
    "	from player_has_round, round, player" +
"	where round.idround = ?" +
// mod 11/06/2017 "	and round.RoundGame = 'SCRAMBLE'" +
    "	and InscriptionIdRound = round.idround" +
    "   and player.idplayer = InscriptionIdPlayer"
    ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, round.getIdround()); 
         utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();

    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet RoundPlayersList has " + rs.getRow() + " lines.");
         if(rs.getRow() == 0)
         { String msg = "no players for RoundPlayersList";
            LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
      //          return null;
         }     
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
	while(rs.next()){
            Player p = new Player();
            p = entite.Player.mapPlayer(rs);
         //   p.setPlayer(p);
          
    //      cc.setIdplayer(rs.getInt("InscriptionIdPlayer") );
    //                    cc.setPlayerLastName(rs.getString("playerlastname"));
    //                    cc.setPlayerEmail(rs.getString("playeremail"));
    //                    cc.setPlayerLanguage(rs.getString("playerlanguage"));
    //                    cc.setPlayerCity(rs.getString("playercity"));
           LOG.info("idplayer = " + p.getIdplayer());
		//store all data into a List
		liste.add(p);
           LOG.info("cc size = " + liste.size());
		}
//LOG.debug(" -- query 5= listcc = "  );
//LOG.debug(" -- query 5= playercity = " + liste.get(0).getPlayerCity() );

    return liste;

//}catch (LCCustomException e){
  //  LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
//    return null;    
}catch (NullPointerException npe){
    String msg = "NullPointerException in RoundPlayersList() " + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (SQLException e){
    String msg = "SQL Exception in RoundPlayersList : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in RoundPlayersList() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
       LOG.debug("escaped to listParticipants repetition with lazy loading");
    return liste;  //plusieurs fois ??
    }
} //end method

    public static List<Player> getListe() {
        return liste;
    }

    public static void setListe(List<Player> liste) {
        RoundPlayersList.liste = liste;
    }
    
  public static void main(String[] args) throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
  //  Player player = new Player();
  //  player.setIdplayer(324713);
   Round round = new Round(); 
   round.setIdround(437);
   
  //  Club club = new Club();
  //  club.setIdclub(1006);
    List<Player> p1 = new RoundPlayersList().list(round, conn);
        LOG.info("Inscription list = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main

} //end class