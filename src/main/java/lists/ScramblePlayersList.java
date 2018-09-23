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

public class ScramblePlayersList implements interfaces.Log
{  //en réalité vaut pour tous games !
    private static List<Player> liste = null;
    
public List<Player> listAllParticipants(final Round round ,final Connection conn) throws SQLException 
{   
if(liste == null)
{    LOG.debug("starting ScramblePlayersList "  );
     LOG.debug("starting ScramblePlayersList for round =  " + round.toString()  );
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{   
    String query =
     " SELECT playerLastName, playerlanguage, playeremail, playercity, player_has_round.player_idplayer,"
     + " round.RoundGame, round.idround, round.RoundGame,"
     + " round.RoundCompetition, round.roundTeam" +
"	from player_has_round, round, player" +
"	where round.idround = ?" +
// mod 11/06/2017 "	and round.RoundGame = 'SCRAMBLE'" +
"	and player_has_round.round_idround = round.idround" +
    "   and player.idplayer = player_has_round.player_idplayer"
    ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, round.getIdround()); 
         utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();

    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet ScramblePlayersList has " + rs.getRow() + " lines.");
         if(rs.getRow() == 0)
         { String msg = "no players for ScramblePlayersList";
            LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
      //          return null;
         }     
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
		while(rs.next())
                {
			Player cc = new Player();
             //           cc = null;
                        cc.setIdplayer(rs.getInt("player_has_round.player_idplayer") );
                        cc.setPlayerLastName(rs.getString("playerlastname"));
                        cc.setPlayerEmail(rs.getString("playeremail"));
                        cc.setPlayerLanguage(rs.getString("playerlanguage"));
                        cc.setPlayerCity(rs.getString("playercity"));
                         LOG.info("idplayer = " + cc.getIdplayer());
			//store all data into a List
			liste.add(cc);
                         LOG.info("cc size = " + liste.size());
		}
//LOG.debug(" -- query 5= listcc = "  );
//LOG.debug(" -- query 5= playercity = " + liste.get(0).getPlayerCity() );

    return liste;

//}catch (LCCustomException e){
  //  String msg = " SQL Exception in getScoreCardList1() " + e;
  //  LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
//    return null;    
}catch (NullPointerException npe){
    String msg = "NullPointerException in ScramblePlayersList() " + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (SQLException e){
    String msg = "SQL Exception in ScramblePlayersList : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in ScramblePlayersList() " + ex;
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
        ScramblePlayersList.liste = liste;
    }
} //end class