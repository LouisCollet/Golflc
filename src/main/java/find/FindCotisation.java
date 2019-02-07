package find;

import entite.Club;
import entite.Cotisation;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindCotisation implements interfaces.Log, interfaces.GolfInterface
{
    final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    
public Cotisation find(final Player player, final Club club, final Round round, final Connection conn) throws SQLException{
     String CLASSNAME2 = Thread.currentThread().getStackTrace()[1].getClassName(); 
    LOG.info("entering : " + CLASSNAME2); 
    LOG.info("starting findCotisation.find for player = " + player.toString());
    LOG.info("starting findCotisation.find for round = " + round.toString());
    LOG.info("starting findCotisation.find for club = " + club.toString());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{ 
    String c= utils.DBMeta.listMetaColumnsLoad(conn, "cotisation");
    String query = 
    " SELECT " 
       + c +
"	FROM cotisation " +
"       WHERE cotisation.CotisationIdPlayer = ?" +
"	  AND cotisation.CotisationIdClub = ?" +
"	  AND DATE(?) BETWEEN (cotisationStartDate AND cotisationEndDate)" +
"	AND cotisation.CotisationStatus = 'Y' "
     ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    ps.setInt(2, club.getIdclub());
    ps.setDate(3, utils.LCUtil.LocalDateTimetoSqlDate(round.getRoundDate()));
 //   ps.setTimestamp(3, round.getRoundDate().toLocalDateTime());
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindCotisation has " + rs.getRow() + " lines.");
         if(rs.getRow() == 0){
            String err = "Vous n'êtes PAS membre de ce club - Vous devez payer un greenfee !" ;//+ cotisation.getIdclub();
            LOG.info(err);
            LCUtil.showMessageFatal(err);
      //         throw new Exception(err + " / " + rs.getRow() );
            Cotisation cotisation = new Cotisation();
            cotisation.setStatus("N");
            return cotisation;
         }
         //rs.last(); //on récupère le numéro de la ligne
        if(rs.getRow() > 1){
            String err = " -- abnormal situation More than 1 cotisation paid ? =";
            LOG.error(err);
            LCUtil.showMessageFatal(err);
            throw new Exception(err + " / " + rs.getRow() );
        }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.info("just before while ! ");
        Cotisation cotisation = null;
	while(rs.next())
        {
             cotisation = entite.Cotisation.mapCotisation(rs);
	}
        return cotisation;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + CLASSNAME2 + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
  //  DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

public static void main(String[] args) throws SQLException, Exception // testing purposes
{
  //  LOG.info("Input main = " + s);
    DBConnection dbc = new DBConnection();
Connection conn = dbc.getConnection();
    Player player = new Player();
  //  Round round =new Round(); 
player.setIdplayer(324713);
//round.setIdround(260);
FindPlayer fp = new FindPlayer();
  //  String str = pc.checkPassword(uuid, conn);
Player p1 = fp.findPlayer(player.getIdplayer(), conn);
LOG.info("player found = " + p1.toString());
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class

