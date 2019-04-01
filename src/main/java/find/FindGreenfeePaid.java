package find;

import entite.Club;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindGreenfeePaid implements interfaces.GolfInterface{

    final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    
public boolean find(final Player player, final Club club, final Round round, final Connection conn) throws SQLException{
     String CLASSNAME2 = Thread.currentThread().getStackTrace()[1].getClassName(); 
    LOG.info("entering : " + CLASSNAME2);

    LOG.info("starting FindGreenfeePaid.find for player = " + player.toString());
    LOG.info("starting FindGreenfeePaid.find for round = " + round.toString());
    LOG.info("starting FindGreenfeePaid.find for club = " + club.toString());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{ 
    String g = utils.DBMeta.listMetaColumnsLoad(conn,"payments_greenfee");
    String query = 
    " SELECT " + g +
    " FROM payments_greenfee " +
    " WHERE GreenfeeIdRound = ?" +
    "   AND GreenfeeIdPlayer = ?" +
 //   "   AND DATE(?) = greenfee.GreenfeeRoundDate" +
    "   AND GreenfeeStatus = 'Y' "
            ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, round.getIdround());
    ps.setInt(2, player.getIdplayer());
 //   ps.setDate(3, utils.LCUtil.LocalDateTimetoSqlDate(round.getRoundDate()));
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindGreenfeePaid has " + rs.getRow() + " lines.");
         if(rs.getRow() == 0){
        //    String err = " !" ;//+ cotisation.getIdclub();
            String msg = LCUtil.prepareMessageBean("greenfee.notfound");
            msg = msg + player.getPlayerLastName() + " / " + player.getIdplayer();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            return false;
         }
         //rs.last(); //on récupère le numéro de la ligne
        if(rs.getRow() > 1){
            String err = "Abnormal technical situation More than 1 cotisation paid ? =";
            LOG.error(err);
            LCUtil.showMessageFatal(err);
            return false;
        }
        if(rs.getRow() == 1){
            String msg = "Vous avez payé le greenfee, merci !";
            LOG.info(msg);
   //         LCUtil.showMessageInfo(msg);
            return true;
        }
        
/*        rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.info("just before while ! ");
        Cotisation cotisation = null;
	while(rs.next())
        {
             cotisation = entite.Cotisation.mapCotisation(rs);
	}
        return true;*/
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch (Exception ex){
    String msg = "Exception in " + CLASSNAME2 + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return false;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
return false;
}//end method

public static void main(String[] args) throws SQLException, Exception{ // testing purposes

Connection conn = new DBConnection().getConnection();
  Player player = new Player();
  player.setIdplayer(324713);
  Round round =new Round();
  round.setIdround(260);
  Club club = new Club(); 
 // ici
    Boolean b = new FindGreenfeePaid().find(player, club, round,conn);
        LOG.info("player fidgreenfeePaid = " + b.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
    
} // end Class

