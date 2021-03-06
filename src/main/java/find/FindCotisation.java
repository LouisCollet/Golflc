package find;

import entite.Club;
import entite.Cotisation;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import load.LoadClub;
import load.LoadRound;
import utils.DBConnection;
import utils.LCUtil;

public class FindCotisation implements interfaces.Log, interfaces.GolfInterface{
    
    final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    static List<Cotisation> liste = null;
    
public Cotisation find(final Player player, final Club club, final Round round, final Connection conn) throws SQLException{
     String CLASSNAME2 = Thread.currentThread().getStackTrace()[1].getClassName(); 
    LOG.info("entering : " + CLASSNAME2);

    LOG.info("starting findCotisation.find for player = " + player);
    LOG.info("starting findCotisation.find for round = " + round);
    LOG.info("starting findCotisation.find for club = " + club);
    PreparedStatement ps = null;
    ResultSet rs = null;
    Cotisation cotisation = new Cotisation();
    

try{ 
    String c = utils.DBMeta.listMetaColumnsLoad(conn, "payments_cotisation");
    String query = 
    " SELECT " + c +
"	FROM payments_cotisation " +
"       WHERE CotisationIdPlayer = ?" +
"	  AND CotisationIdClub = ?" +
"	  AND DATE(?) BETWEEN (cotisationStartDate AND cotisationEndDate)" +
"	AND CotisationStatus = 'Y' "
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
        //    String err = " !" ;//+ cotisation.getIdclub();
            String msg = LCUtil.prepareMessageBean("cotisation.notfound");
            LOG.info(msg);
     //       LCUtil.showMessageFatal(msg);
        //    Cotisation cotisation = new Cotisation();
            cotisation.setStatus("NF");
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
     
	while(rs.next()){
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

public List<Cotisation> findAll(final Player player, final Club club, final Round round, final Connection conn) throws SQLException{
     String CLASSNAME2 = Thread.currentThread().getStackTrace()[1].getClassName(); 
    LOG.info("entering : " + CLASSNAME2);

    LOG.info("starting findCotisation.find for player = " + player);
    LOG.info("starting findCotisation.find for round = " + round);
    LOG.info("starting findCotisation.find for club = " + club);
    PreparedStatement ps = null;
    ResultSet rs = null;
    Cotisation cotisation = new Cotisation();
try{ 
    String c = utils.DBMeta.listMetaColumnsLoad(conn, "payments_cotisation");
    String query = 
    " SELECT " + c +
"	FROM payments_cotisation " +
"       WHERE CotisationIdPlayer = ?" +
"         AND DATE(?) >= cotisationStartDate" +
"         AND DATE(?) <= cotisationEndDate" +
"	AND CotisationStatus = 'Y' "
     ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    ps.setDate(2, utils.LCUtil.LocalDateTimetoSqlDate(round.getRoundDate()));
    ps.setDate(3, utils.LCUtil.LocalDateTimetoSqlDate(round.getRoundDate())); // new 
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindCotisation has " + rs.getRow() + " lines.");
         if(rs.getRow() == 0){
            String err = LCUtil.prepareMessageBean("cotisation.notfound");
            LOG.info(err);
     //       LCUtil.showMessageFatal(msg);
        //    Cotisation cotisation = new Cotisation();
            cotisation.setStatus("NF");
            return liste;
         }
         //rs.last(); //on récupère le numéro de la ligne
 //       if(rs.getRow() > 1){
 //           String err = " -- abnormal situation More than 1 cotisation paid ? =";
 //           LOG.error(err);
 //           LCUtil.showMessageFatal(err);
 //           throw new Exception(err + " / " + rs.getRow() );
 //       }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.info("just before while ! ");
      liste = new ArrayList<>();
	while(rs.next()){
             liste.add(entite.Cotisation.mapCotisation(rs));
	}
        return liste;
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
    DBConnection.closeQuietly(null, null, rs, ps);
}
}//end method

public static void main(String[] args) throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    
    Round round = new Round(); 
    round.setIdround(434); // 260
    round = new LoadRound().load(round, conn);
    
    Club club = new Club();
    club.setIdclub(1006);
    club = new LoadClub().load(club, conn);
    
    Cotisation p1c = new FindCotisation().find(player, club, round, conn);
    LOG.info("cotisation unique found = " + p1c); //.toString());
    
    
    List<Cotisation> p1 = new FindCotisation().findAll(player, club, round, conn);
        LOG.info("all cotisations found = " + p1); //.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class