package find;

import entite.Creditcard;
import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindCreditcard implements interfaces.Log, interfaces.GolfInterface{

    final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    
public Creditcard find(final Player player, final Connection conn) throws SQLException{
     String CLASSNAME2 = Thread.currentThread().getStackTrace()[1].getClassName(); 
    LOG.info("entering : " + CLASSNAME2);

    LOG.info("starting findCreditcard.find for player = " + player.toString());
    PreparedStatement ps = null;
    ResultSet rs = null;
    Creditcard creditcard = new Creditcard();
try{ 
    String c = utils.DBMeta.listMetaColumnsLoad(conn, "creditcard");
    String query = 
    " SELECT " 
       + c +
"	FROM creditcard " +
"       WHERE CreditcardIdPlayer = ?"
// + "	  AND CotisationIdClub = ?" +
//"	  AND DATE(?) BETWEEN (cotisationStartDate AND cotisationEndDate)" +
//"	AND CotisationStatus = 'Y' "
     ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
//    ps.setInt(2, club.getIdclub());
 //   ps.setDate(3, utils.LCUtil.LocalDateTimetoSqlDate(round.getRoundDate()));
 //   ps.setTimestamp(3, round.getRoundDate().toLocalDateTime());
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindCreditcard has " + rs.getRow() + " lines.");
         if(rs.getRow() == 0){
        //    String err = " !" ;//+ cotisation.getIdclub();
            String msg = LCUtil.prepareMessageBean("creditcard.notfound");
            LOG.info(msg);
     //       LCUtil.showMessageFatal(msg);
        //    Cotisation cotisation = new Cotisation();
       //     cotisation.setStatus("NF");
            return creditcard;
         }
         //rs.last(); //on récupère le numéro de la ligne
        if(rs.getRow() > 1){
            String err = " -- abnormal situation More than 1 creditcard known ? =";
            LOG.error(err);
            LCUtil.showMessageFatal(err);
            throw new Exception(err + " / " + rs.getRow() );
        }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
	while(rs.next()){
             creditcard = entite.Creditcard.mapCreditcard(rs);
	}
     return creditcard;
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
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Creditcard cc = new FindCreditcard().find(player, conn);
        LOG.info("creditcard found = " + cc.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
    
} // end Class

