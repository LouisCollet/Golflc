package find;

import entite.Club;
import entite.Cotisation;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import read.ReadRound;
import utils.DBConnection;
import utils.LCUtil;

public class FindCotisationAtRoundDate implements interfaces.Log, interfaces.GolfInterface{
    
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    static List<Cotisation> liste = null;
    
public Cotisation find(final Player player, final Club club, final Round round, final Connection conn) throws SQLException{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("entering : " + methodName);
    LOG.debug(" for player = " + player);
    LOG.debug(" round = " + round);
    LOG.debug(" for club = " + club);
    
    Cotisation cotisation = new Cotisation();
    // new 15-09-2021
    if(club.getIdclub().equals(1159)){  // Whistling Straits Ryder Cup 2021   accès non payant !!
        cotisation.setStatus("Y");
        return cotisation;
    }
    PreparedStatement ps = null;
    ResultSet rs = null;
    
try{ 
 /*   String query = """
    SELECT *
    FROM payments_cotisation
    WHERE CotisationIdPlayer = ?
      AND CotisationIdClub = ?
      AND DATE(?) BETWEEN DATE(cotisationStartDate) AND DATE(cotisationEndDate)
      AND CotisationStatus = 'Y'
    """  ;
  */  
    final String query = """
    SELECT *
    FROM payments_cotisation
    WHERE CotisationIdPlayer = ?
      AND CotisationIdClub = ?
      AND ? BETWEEN cotisationStartDate AND cotisationEndDate
      AND CotisationStatus = 'Y'
    """  ;
    
    
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    ps.setInt(2, club.getIdclub());
  //  ps.setDate(3, java.sql.Date.valueOf(round.getRoundDate().toLocalDate()));
    ps.setTimestamp(3,Timestamp.valueOf(round.getRoundDate()));
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    int i = 0;
	while(rs.next()){
            i++;
        //     cotisation = entite.Cotisation.map(rs);
             cotisation = entite.Cotisation.map(rs);
	}
        LOG.debug("i = " + i);
   //    if(cotisation.getIdclub() == null){
       if(i == 0){
            String msg = LCUtil.prepareMessageBean("cotisation.notfound");
            LOG.info(msg);
  //          LCUtil.showMessageInfo(msg);
            cotisation.setStatus("NF");
        }else{
           LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
           LOG.debug("cotisation found");
           cotisation.setStatus("Y");
        }     
        return cotisation;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
}//end method

public List<Cotisation> findAll(final Player player, final Club club, final Round round, final Connection conn) throws SQLException{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    LOG.debug("entering : " + methodName);

    LOG.debug("starting findCotisation.find for player = " + player);
    LOG.debug("starting findCotisation.find for round = " + round);
    LOG.debug("starting findCotisation.find for club = " + club);
    PreparedStatement ps = null;
    ResultSet rs = null;
    Cotisation cotisation = new Cotisation();
try{ 
    String c = utils.DBMeta.listMetaColumnsLoad(conn, "payments_cotisation");
    final String query = 
    " SELECT " + c +
"	FROM payments_cotisation " +
"       WHERE CotisationIdPlayer = ?" +
"         AND DATE(?) >= cotisationStartDate" +
"         AND DATE(?) <= cotisationEndDate" +
"	AND CotisationStatus = 'Y' "
     ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
 //   ps.setDate(2, utils.LCUtil.LocalDateTimetoSqlDate(round.getRoundDate()));
    ps.setDate(2, java.sql.Date.valueOf(round.getRoundDate().toLocalDate()));
  //  ps.setDate(3, utils.LCUtil.LocalDateTimetoSqlDate(round.getRoundDate())); // new 
    ps.setDate(3, java.sql.Date.valueOf(round.getRoundDate().toLocalDate()));
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        //LOG.debug("just before while ! ");
      liste = new ArrayList<>();
      int i = 0;
	while(rs.next()){
            i++;
        //     liste.add(entite.Cotisation.map(rs));
             liste.add(entite.Cotisation.map(rs)); // mod 26/09/2022
	}
     if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
  //       return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
     }  
  return liste;
}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
}//end method

void main() throws SQLException, Exception {
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324715);
    Round round = new Round(); 
    round.setIdround(699);
    round = new ReadRound().read(round, conn);
    Club club = new Club();
    club.setIdclub(1006);
 //   club = new LoadClub().load(club, conn);
    Cotisation cotisation = new FindCotisationAtRoundDate().find(player, club, round, conn);
       LOG.debug("cotisation unique found = " + cotisation); //.toString());
    
    
 //   List<Cotisation> p1 = new FindCotisation().findAll(player, club, round, conn);
 //       LOG.debug("all cotisations found = " + p1); //.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end Class