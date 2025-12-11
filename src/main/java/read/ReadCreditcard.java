package read;

import entite.Creditcard;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class ReadCreditcard implements interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public Creditcard read(final Player player, final Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug(" ... entering " + methodName);
        LOG.debug("for Idplayer = " + player.getIdplayer());
    PreparedStatement ps = null;
    ResultSet rs = null;
    Creditcard creditcard = new Creditcard();
try{ 
    String query = """
     SELECT *
     FROM creditcard
     WHERE CreditcardIdPlayer = ?
    """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
	while(rs.next()){
             creditcard = entite.Creditcard.map(rs);
	}
    if(creditcard.getCreditcardNumber() == null){
          String msg = prepareMessageBean("creditcard.notfound");
          LOG.debug(msg);
          showMessageInfo(msg);
     }else{
          String msg = LCUtil.prepareMessageBean("creditcard.found")+ creditcard;
          LOG.debug(msg);
  //        LCUtil.showMessageInfo(msg);
     }
     return creditcard;
}catch (SQLException e){
    String msg = "SQL Exception in  " + methodName + " / " + e.toString() + ", SQLState = " + e.getSQLState()
          + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        showMessageFatal(msg);
       return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / "  + ex;
    LOG.error(msg);
    showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
}//end method

void main() throws SQLException, Exception{
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324733);
    Creditcard cc = new ReadCreditcard().read(player, conn);
        LOG.debug("creditcard found = " + cc.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class